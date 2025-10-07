package ar.edu.uade.ecommerce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;

@Component
public class KeycloakClient {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakClient.class);

    // RestTemplate dedicado para token (con timeouts cortos)
    private final RestTemplate restTemplate;

    @Value("${keycloak.token.url:}")
    private String tokenUrl;
    @Value("${keycloak.client-id:}")
    private String clientId;
    @Value("${keycloak.client-secret:}")
    private String clientSecret;

    @Value("${keycloak.token.maxAttempts:3}")
    private int maxAttempts;
    @Value("${keycloak.token.connectTimeoutMs:2000}")
    private int connectTimeoutMs;
    @Value("${keycloak.token.readTimeoutMs:5000}")
    private int readTimeoutMs;

    private String cachedToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public KeycloakClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofMillis(2000).toMillis());
        factory.setReadTimeout((int) Duration.ofMillis(5000).toMillis());
        this.restTemplate = new RestTemplate(factory);
    }

    private Map<String, Object> requestToken() {
        if (tokenUrl == null || tokenUrl.isBlank() || clientId == null || clientId.isBlank()) {
            logger.warn("KeycloakClient no configurado (keycloak.token.url / client-id faltan). No se solicitará token.");
            return null;
        }
        // Asegurar timeouts actualizados si se parametrizaron por properties
        try {
            var rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
            rf.setConnectTimeout(connectTimeoutMs);
            rf.setReadTimeout(readTimeoutMs);
        } catch (Exception ignore) {}

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isBlank()) body.add("client_secret", clientSecret);
        HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(body, headers);
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = (Map<String, Object>) restTemplate.postForObject(tokenUrl, req, Map.class);
        return resp;
    }

    private Map<String, Object> requestTokenWithRetries(int attempts) {
        int attempt = 0;
        while (attempt < attempts) {
            attempt++;
            try {
                Map<String, Object> resp = requestToken();
                if (resp != null) return resp;
            } catch (Exception ex) {
                logger.warn("Intento {} obtener token desde Keycloak falló: {}", attempt, ex.getMessage());
            }
            try { Thread.sleep(200L * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
        }
        return null;
    }

    public synchronized String getClientAccessToken() {
        try {
            if (cachedToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(10))) {
                return cachedToken;
            }
            Map<String, Object> resp = requestTokenWithRetries(maxAttempts);
            if (resp == null) return null;
            Object tokenObj = resp.get("access_token");
            Object expiresObj = resp.get("expires_in");
            if (tokenObj != null) {
                cachedToken = tokenObj.toString();
                long expiresIn = 60 * 5; // default 5min
                if (expiresObj != null) {
                    try { expiresIn = Long.parseLong(String.valueOf(expiresObj)); } catch (Exception ignored) {}
                }
                tokenExpiry = Instant.now().plusSeconds(expiresIn);
                logger.info("Keycloak client token obtenido, expira en {} segundos", expiresObj != null ? expiresObj : expiresIn);
                return cachedToken;
            }
        } catch (Exception ex) {
            logger.warn("No se pudo obtener token desde Keycloak: {}", ex.getMessage());
        }
        return null;
    }

    // Nuevo: método que devuelve token y expiresIn para uso por BackendTokenManager
    public synchronized TokenInfo getClientAccessTokenInfo() {
        try {
            if (cachedToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(10))) {
                long remaining = tokenExpiry.getEpochSecond() - Instant.now().getEpochSecond();
                return new TokenInfo(cachedToken, remaining);
            }
            Map<String, Object> resp = requestTokenWithRetries(maxAttempts);
            if (resp == null) return null;
            Object tokenObj = resp.get("access_token");
            Object expiresObj = resp.get("expires_in");
            if (tokenObj != null) {
                cachedToken = tokenObj.toString();
                long expiresIn = 60 * 5; // default 5min
                if (expiresObj != null) {
                    try { expiresIn = Long.parseLong(String.valueOf(expiresObj)); } catch (Exception ignored) {}
                }
                tokenExpiry = Instant.now().plusSeconds(expiresIn);
                logger.info("Keycloak client token obtenido (info), expira en {} segundos", expiresIn);
                return new TokenInfo(cachedToken, expiresIn);
            }
        } catch (Exception ex) {
            logger.warn("No se pudo obtener token desde Keycloak (info): {}", ex.getMessage());
        }
        return null;
    }

    public static class TokenInfo {
        public final String accessToken;
        public final long expiresInSeconds;
        public TokenInfo(String accessToken, long expiresInSeconds) {
            this.accessToken = accessToken;
            this.expiresInSeconds = expiresInSeconds;
        }
    }
}
