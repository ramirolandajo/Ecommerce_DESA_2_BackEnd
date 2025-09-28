package ar.edu.uade.ecommerce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class ECommerceEventService {
    private static final Logger logger = LoggerFactory.getLogger(ECommerceEventService.class);
    private final CoreApiClient coreApiClient;
    private final KeycloakClient keycloakClient;
    private final BackendTokenManager backendTokenManager;

    // Token estático opcional para entornos de desarrollo (evita que los eventos queden sin firma)
    @Value("${messaging.backend-token:}")
    private String configuredBackendToken;

    public ECommerceEventService(CoreApiClient coreApiClient, KeycloakClient keycloakClient, BackendTokenManager backendTokenManager) {
        this.coreApiClient = coreApiClient;
        this.keycloakClient = keycloakClient;
        this.backendTokenManager = backendTokenManager;
    }

    // Helper: obtiene token client_credentials del backend (cacheado), con fallback a KeycloakClient o token configurado; devuelve null si no se puede obtener
    private String obtainBackendToken(String eventType) {
        try {
            // 1) Si hay token estático configurado, lo usamos (útil en dev)
            if (configuredBackendToken != null && !configuredBackendToken.isBlank()) {
                logger.debug("Usando token backend configurado (static) para evento {}", eventType);
                return configuredBackendToken.trim();
            }
            // 2) Intentar obtener token cacheado del BackendTokenManager
            String token = backendTokenManager.getToken();
            if (token != null) return token;
            // 3) Fallback: pedir directamente a Keycloak
            token = keycloakClient.getClientAccessToken();
            if (token != null) {
                logger.info("Backend token obtenido vía KeycloakClient (fallback) para evento {}", eventType);
                return token;
            }
        } catch (Exception ex) {
            logger.warn("Error al obtener token backend para evento {}: {}", eventType, ex.getMessage());
        }
        logger.warn("No se pudo obtener token de backend para evento {}", eventType);
        return null;
    }

    // Método genérico para enviar eventos arbitrarios
    public void emitRawEvent(String type, Object payload) {
        String origin = obtainBackendToken(type);
        if (origin == null) {
            logger.warn("No se pudo obtener token de backend (Keycloak). Evento '{}' no será enviado.", type);
            return;
        }
        CoreEvent event = new CoreEvent(type, payload, origin);
        logger.info("Emitiendo evento genérico: {} (origin set) payloadType={}", type, payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    // Eventos de compra
    public void emitPurchasePending(Integer purchaseId, Map<String, Object> user, Map<String, Object> cart) {
        emitPurchaseEvent("POST: Compra pendiente", purchaseId, user, cart, "PENDING");
    }

    public void emitPurchaseConfirmed(Integer purchaseId, Map<String, Object> user, Map<String, Object> cart) {
        emitPurchaseEvent("POST: Compra confirmada", purchaseId, user, cart, "CONFIRMED");
    }

    public void emitPurchaseCancelled(Integer purchaseId, Map<String, Object> user, Map<String, Object> cart) {
        emitPurchaseEvent("DELETE: Compra cancelada", purchaseId, user, cart, "CANCELLED");
    }

    private void emitPurchaseEvent(String type, Integer purchaseId, Map<String, Object> user, Map<String, Object> cart, String status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("purchaseId", purchaseId);
        payload.put("user", user);
        payload.put("cart", cart);
        payload.put("status", status);

        String origin = obtainBackendToken(type);
        if (origin == null) {
            logger.warn("No se pudo obtener token de backend para evento de compra '{}'. Abortando envío.", type);
            return;
        }
        CoreEvent event = new CoreEvent(type, payload, origin);
        logger.info("Emitiendo evento de compra: {} payloadType={}", type, payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    // Review
    public void emitReviewCreated(String message, Float rateUpdated) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("rateUpdated", rateUpdated);
        String origin = obtainBackendToken("POST: Review creada");
        if (origin == null) {
            logger.warn("No se pudo obtener token de backend para evento review. Abortando envío.");
            return;
        }
        CoreEvent event = new CoreEvent("POST: Review creada", payload, origin);
        logger.info("Emitiendo evento de review: payloadType={}", payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    // Favoritos
    public void emitAddFavorite(String productCode, Long id, String nombre) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", productCode);
        payload.put("id", id);
        payload.put("nombre", nombre);
        String origin = obtainBackendToken("POST: Producto agregado a favoritos");
        if (origin == null) {
            logger.warn("No se pudo obtener token de backend para evento add favorite. Abortando envío.");
            return;
        }
        CoreEvent event = new CoreEvent("POST: Producto agregado a favoritos", payload, origin);
        logger.info("Emitiendo evento add favorite: payloadType={}", payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    public void emitRemoveFavorite(String productCode, Long id, String nombre) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", productCode);
        payload.put("id", id);
        payload.put("nombre", nombre);
        String origin = obtainBackendToken("DELETE: Producto quitado de favoritos");
        if (origin == null) {
            logger.warn("No se pudo obtener token de backend para evento remove favorite. Abortando envío.");
            return;
        }
        CoreEvent event = new CoreEvent("DELETE: Producto quitado de favoritos", payload, origin);
        logger.info("Emitiendo evento remove favorite: payloadType={}", payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    // Trata de obtener el token bearer del contexto de seguridad o de la petición HTTP
    // (seguimos manteniendo este método para usos que necesiten el token del usuario)
    private String getBearerToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                Object creds = auth.getCredentials();
                if (creds instanceof String) {
                    String s = ((String) creds).trim();
                    if (s.startsWith("Bearer ")) s = s.substring(7).trim();
                    if (!s.isBlank()) {
                        logger.debug("Obtenido token desde SecurityContext credentials (length={})", s.length());
                        return s;
                    } else {
                        logger.debug("SecurityContext credentials estaban vacíos, haciendo fallback");
                    }
                }
            }
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpServletRequest req = attr.getRequest();
                String authHeader = req.getHeader("Authorization");
                if (authHeader != null) {
                    String v = authHeader.trim();
                    if (v.startsWith("Bearer ")) v = v.substring(7).trim();
                    if (!v.isBlank()) {
                        logger.debug("Obtenido token desde header Authorization (length={})", v.length());
                        return v;
                    } else {
                        logger.debug("Header Authorization presente pero vacío, haciendo fallback");
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("No se pudo obtener token bearer del contexto: {}", ex.getMessage());
        }
        // No hacemos fallback aquí porque para eventos usamos BackendTokenManager
        return null;
    }
}
