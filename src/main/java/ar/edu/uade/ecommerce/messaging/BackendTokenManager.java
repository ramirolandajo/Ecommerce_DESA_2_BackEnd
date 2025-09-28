package ar.edu.uade.ecommerce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.Instant;

@Component
public class BackendTokenManager {
    private static final Logger logger = LoggerFactory.getLogger(BackendTokenManager.class);

    private final KeycloakClient keycloakClient;

    // Cached token + expiry
    private volatile String token;
    private volatile Instant expiry = Instant.EPOCH;

    // Small lock to coalesce refreshes
    private final Object lock = new Object();

    // Seconds before expiry to proactively refresh
    private final long refreshBeforeSeconds = 30;

    @Autowired
    public BackendTokenManager(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    /**
     * Obtiene el token client_credentials cached; si está vencido o ausente, solicita uno nuevo.
     */
    public String getToken() {
        try {
            Instant now = Instant.now();
            if (token != null && now.isBefore(expiry.minusSeconds(refreshBeforeSeconds))) {
                return token;
            }
            synchronized (lock) {
                now = Instant.now();
                if (token != null && now.isBefore(expiry.minusSeconds(refreshBeforeSeconds))) {
                    return token;
                }
                // Pedimos token con info (token + expires_in)
                KeycloakClient.TokenInfo info = keycloakClient.getClientAccessTokenInfo();
                if (info != null && info.accessToken != null) {
                    token = info.accessToken;
                    expiry = Instant.now().plusSeconds(info.expiresInSeconds);
                    logger.info("BackendTokenManager obtuvo/actualizó token, expira en {} segundos", info.expiresInSeconds);
                    return token;
                }
                // Si no obtuvimos info, si el token aún es válido (incluso si está dentro del umbral), lo devolvemos
                if (token != null && Instant.now().isBefore(expiry)) {
                    logger.warn("No se pudo refrescar token desde Keycloak, usando token en cache aún válido");
                    return token;
                }
                logger.warn("No hay token disponible (Keycloak no configurado o fallo al obtener token)");
                return null;
            }
        } catch (Exception ex) {
            logger.warn("Error obteniendo token desde BackendTokenManager: {}", ex.getMessage());
            if (token != null && Instant.now().isBefore(expiry)) return token;
            return null;
        }
    }

    // Precargar token al iniciar la aplicación
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("BackendTokenManager: Application ready, precargando token backend...");
        String t = getToken();
        if (t == null) {
            logger.warn("BackendTokenManager: No se pudo precargar token al iniciar; se intentará en primer uso");
        } else {
            logger.info("BackendTokenManager: token precargado exitosamente (len={})", t.length());
        }
    }

    // Tarea periódica para refrescar token antes de expirar (ejecuta cada minuto)
    @Scheduled(fixedDelayString = "PT60S")
    public void refreshIfNearExpiry() {
        try {
            Instant now = Instant.now();
            if (expiry.minusSeconds(60).isBefore(now)) {
                logger.debug("BackendTokenManager: token próximo a expirar o ausente, refrescando...");
                String t = getToken();
                if (t == null) {
                    logger.warn("BackendTokenManager: refresh programado falló, token sigue nulo");
                } else {
                    logger.debug("BackendTokenManager: refresh programado completado (len={})", t.length());
                }
            }
        } catch (Exception ex) {
            logger.error("BackendTokenManager: error en refreshIfNearExpiry: {}", ex.getMessage(), ex);
        }
    }
}
