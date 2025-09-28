package ar.edu.uade.ecommerce.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/messaging")
public class MessagingStatusController {

    private final BackendTokenManager backendTokenManager;
    private final KeycloakClient keycloakClient;

    @Value("${messaging.backend-token:}")
    private String configuredBackendToken;

    public MessagingStatusController(BackendTokenManager backendTokenManager, KeycloakClient keycloakClient) {
        this.backendTokenManager = backendTokenManager;
        this.keycloakClient = keycloakClient;
    }

    @GetMapping("/backend-token-status")
    public Map<String, Object> status() {
        Map<String, Object> m = new HashMap<>();
        boolean hasStatic = configuredBackendToken != null && !configuredBackendToken.isBlank();
        m.put("configuredBackendTokenPresent", hasStatic);
        if (hasStatic) {
            String t = configuredBackendToken.trim();
            m.put("configuredBackendTokenPreview", t.length() > 8 ? t.substring(0,8) + "..." : t);
        }
        try {
            String cached = backendTokenManager.getToken();
            m.put("backendTokenCached", cached != null);
            if (cached != null) m.put("backendTokenPreview", cached.length() > 8 ? cached.substring(0,8) + "..." : cached);
        } catch (Exception ex) {
            m.put("backendTokenCachedError", ex.getMessage());
        }
        try {
            KeycloakClient.TokenInfo info = keycloakClient.getClientAccessTokenInfo();
            m.put("keycloakClientAvailable", info != null);
            if (info != null) {
                m.put("keycloakAccessTokenPreview", info.accessToken.length() > 8 ? info.accessToken.substring(0,8) + "..." : info.accessToken);
                m.put("keycloakAccessTokenExpiresIn", info.expiresInSeconds);
            }
        } catch (Exception ex) {
            m.put("keycloakClientError", ex.getMessage());
        }
        return m;
    }
}

