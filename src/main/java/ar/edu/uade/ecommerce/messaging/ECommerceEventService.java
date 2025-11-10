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
    private final ar.edu.uade.ecommerce.Repository.EventRepository eventRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    // Nombre del módulo que origina el evento: por defecto usa el client_id de Keycloak (ecommerce-app)
    @Value("${messaging.origin-module:${keycloak.client-id:${spring.application.name:Ecommerce}}}")
    private String originModuleName;

    public ECommerceEventService(CoreApiClient coreApiClient,
                                 KeycloakClient keycloakClient,
                                 BackendTokenManager backendTokenManager,
                                 ar.edu.uade.ecommerce.Repository.EventRepository eventRepository,
                                 com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.coreApiClient = coreApiClient;
        this.backendTokenManager = backendTokenManager;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
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

    // Persistencia local del evento (tabla event). Guarda solo type/payload (JSON)/timestamp.
    private void persistLocalEvent(String type, Object payload) {
        try {
            String payloadJson;
            if (payload == null) {
                payloadJson = null;
            } else if (payload instanceof String s) {
                payloadJson = s;
            } else {
                payloadJson = objectMapper.writeValueAsString(payload);
            }
            ar.edu.uade.ecommerce.Entity.Event e = new ar.edu.uade.ecommerce.Entity.Event(type, payloadJson);
            eventRepository.save(e);
        } catch (Exception ex) {
            logger.warn("No se pudo persistir localmente el evento '{}': {}", type, ex.getMessage());
        }
    }

    // Método genérico para enviar eventos arbitrarios
    public void emitRawEvent(String type, Object payload) {
        ensureBackendTokenAvailable(type); // calentamos/validamos token; el envío sigue y el interceptor añadirá Authorization si lo obtuvo
        // Persistir primero
        persistLocalEvent(type, payload);
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
        if (cart != null) {
            sanitizeCartItems(cart);
        }
        payload.put("cart", cart);

        String effectiveStatus = "POST: Compra pendiente".equals(type)
                ? "PENDING"
                : (status == null || status.isBlank() ? "PENDING" : status);

        payload.put("status", effectiveStatus);
        logger.debug("EmitPurchaseEvent tipo='{}' statusCalculado='{}' payload={}", type, effectiveStatus, payload);

        ensureBackendTokenAvailable(type);
        persistLocalEvent(type, payload);

        CoreEvent event = new CoreEvent(type, payload, originModuleName);

        coreApiClient.sendEvent(event);
    }

    // Review
    public void emitReviewCreated(Integer productCode, String message, Float rateUpdated) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", productCode);
        payload.put("message", message);
        payload.put("rateUpdated", rateUpdated);
        ensureBackendTokenAvailable("POST: Review creada");
        // Persistir primero
        persistLocalEvent("POST: Review creada", payload);
        CoreEvent event = new CoreEvent("POST: Review creada", payload, originModuleName);
        logger.info("Emitiendo evento de review: origin={} payloadType={}", originModuleName, payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    // Favoritos
    public void emitAddFavorite(String productCode, Long id, String nombre) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", productCode);
        payload.put("nombre", nombre);
        ensureBackendTokenAvailable("POST: Producto agregado a favoritos");
        // Persistir primero
        persistLocalEvent("POST: Producto agregado a favoritos", payload);
        CoreEvent event = new CoreEvent("POST: Producto agregado a favoritos", payload, originModuleName);
        logger.info("Emitiendo evento add favorite: origin={} payloadType={}", originModuleName, payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    public void emitRemoveFavorite(String productCode, Long id, String nombre) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", productCode);
        payload.put("nombre", nombre);
        ensureBackendTokenAvailable("DELETE: Producto quitado de favoritos");
        // Persistir primero
        persistLocalEvent("DELETE: Producto quitado de favoritos", payload);
        CoreEvent event = new CoreEvent("DELETE: Producto quitado de favoritos", payload, originModuleName);
        logger.info("Emitiendo evento remove favorite: origin={} payloadType={}", originModuleName, payload != null ? payload.getClass().getSimpleName() : "null");
        coreApiClient.sendEvent(event);
    }

    // Helper: elimina 'productId' de los items del carrito y estandariza clave 'items' o 'cartItems'.
    @SuppressWarnings("unchecked")
    private void sanitizeCartItems(Map<String, Object> cart) {
        if (cart == null) return;
        Object itemsObj = cart.get("items");
        if (!(itemsObj instanceof java.util.List)) {
            // Intentar con 'cartItems' si 'items' no está
            itemsObj = cart.get("cartItems");
        }
        if (itemsObj instanceof java.util.List<?> rawList) {
            java.util.List<java.util.Map<String, Object>> sanitized = new java.util.ArrayList<>();
            for (Object o : rawList) {
                if (o instanceof java.util.Map<?, ?> m) {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    m.forEach((k, v) -> item.put(String.valueOf(k), v));
                    // eliminar productId si existe
                    item.remove("productId");
                    // mantener productCode tal como venga
                    sanitized.add(item);
                }
            }
            // Normalizar clave a 'items'
            cart.remove("cartItems");
            cart.put("items", sanitized);
        }
    }
}
