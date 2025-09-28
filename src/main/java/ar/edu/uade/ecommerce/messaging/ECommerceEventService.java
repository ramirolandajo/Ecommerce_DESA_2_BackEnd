package ar.edu.uade.ecommerce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ECommerceEventService {
    private static final Logger logger = LoggerFactory.getLogger(ECommerceEventService.class);
    private final CoreApiClient coreApiClient;
    private final BackendTokenManager backendTokenManager;

    // Nombre del módulo que origina el evento: por defecto usa el client_id de Keycloak (ecommerce-app)
    @Value("${messaging.origin-module:${keycloak.client-id:${spring.application.name:Ecommerce}}}")
    private String originModuleName;

    public ECommerceEventService(CoreApiClient coreApiClient, KeycloakClient keycloakClient, BackendTokenManager backendTokenManager) {
        this.coreApiClient = coreApiClient;
        this.backendTokenManager = backendTokenManager;
    }

    // Asegura que hay un token backend disponible (para que el interceptor ponga Authorization). No se devuelve el token.
    private boolean ensureBackendTokenAvailable(String context) {
        try {
            String t = backendTokenManager.getToken();
            if (t == null || t.isBlank()) {
                logger.warn("No se pudo obtener token de backend para '{}'. El middleware podría rechazar el evento (401).", context);
                return false;
            }
            return true;
        } catch (Exception ex) {
            logger.warn("Error verificando token backend para '{}': {}", context, ex.getMessage());
            return false;
        }
    }

    // Método genérico para enviar eventos arbitrarios
    public void emitRawEvent(String type, Object payload) {
        ensureBackendTokenAvailable(type); // calentamos/validamos token; el envío sigue y el interceptor añadirá Authorization si lo obtuvo
        CoreEvent event = new CoreEvent(type, payload, originModuleName);
        logger.info("Emitiendo evento genérico: {} (origin={}) payloadType={}", type, originModuleName, payload != null ? payload.getClass().getSimpleName() : "null");
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

        ensureBackendTokenAvailable(type);
        CoreEvent event = new CoreEvent(type, payload, originModuleName);
        logger.info("Emitiendo evento de compra: {} origin={} payloadType={}", type, originModuleName, payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    // Review
    public void emitReviewCreated(String message, Float rateUpdated) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("rateUpdated", rateUpdated);
        ensureBackendTokenAvailable("POST: Review creada");
        CoreEvent event = new CoreEvent("POST: Review creada", payload, originModuleName);
        logger.info("Emitiendo evento de review: origin={} payloadType={}", originModuleName, payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    // Favoritos
    public void emitAddFavorite(String productCode, Long id, String nombre) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", productCode);
        payload.put("id", id);
        payload.put("nombre", nombre);
        ensureBackendTokenAvailable("POST: Producto agregado a favoritos");
        CoreEvent event = new CoreEvent("POST: Producto agregado a favoritos", payload, originModuleName);
        logger.info("Emitiendo evento add favorite: origin={} payloadType={}", originModuleName, payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    public void emitRemoveFavorite(String productCode, Long id, String nombre) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", productCode);
        payload.put("id", id);
        payload.put("nombre", nombre);
        ensureBackendTokenAvailable("DELETE: Producto quitado de favoritos");
        CoreEvent event = new CoreEvent("DELETE: Producto quitado de favoritos", payload, originModuleName);
        logger.info("Emitiendo evento remove favorite: origin={} payloadType={}", originModuleName, payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }
}
