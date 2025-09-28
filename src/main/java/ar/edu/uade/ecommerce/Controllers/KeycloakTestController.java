package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import ar.edu.uade.ecommerce.messaging.KeycloakClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/keycloak")
public class KeycloakTestController {

    @Autowired
    private KeycloakClient keycloakClient;

    @Autowired
    private ECommerceEventService ecommerceEventService;

    // Obtener token client_credentials desde Keycloak (para verificar configuración)
    @GetMapping("/token")
    public ResponseEntity<?> getClientToken() {
        String token = keycloakClient.getClientAccessToken();
        if (token == null) {
            return ResponseEntity.status(500).body(Map.of("ok", false, "message", "No se pudo obtener token desde Keycloak. Revise la configuración."));
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", true);
        resp.put("tokenPreview", token.length() > 64 ? token.substring(0, 64) + "..." : token);
        resp.put("token", token); // se incluye el token completo para pruebas locales (no recomendado en producción)
        return ResponseEntity.ok(resp);
    }

    // Enviar un evento de Compra pendiente de prueba. Si el request body incluye campos, se usan.
    @PostMapping("/send-purchase-pending")
    public ResponseEntity<?> sendPurchasePending(@RequestBody(required = false) Map<String, Object> body) {
        try {
            int purchaseId = 1;
            Map<String, Object> user = Map.of("id", 1L, "name", "Test User", "email", "test@example.com");
            Map<String, Object> cart = Map.of("cartId", 1L, "finalPrice", 100.0f, "cartItems", new java.util.ArrayList<>());

            if (body != null) {
                Object pid = body.get("purchaseId");
                if (pid instanceof Number) purchaseId = ((Number) pid).intValue();

                Object userObj = body.get("user");
                if (userObj instanceof Map) {
                    Map<?,?> tmp = (Map<?,?>) userObj;
                    Map<String, Object> safeUser = new HashMap<>();
                    tmp.forEach((k, v) -> safeUser.put(String.valueOf(k), v));
                    user = safeUser;
                }

                Object cartObj = body.get("cart");
                if (cartObj instanceof Map) {
                    Map<?,?> tmp = (Map<?,?>) cartObj;
                    Map<String, Object> safeCart = new HashMap<>();
                    tmp.forEach((k, v) -> safeCart.put(String.valueOf(k), v));
                    cart = safeCart;
                }
            }

            ecommerceEventService.emitPurchasePending(purchaseId, user, cart);
            return ResponseEntity.ok(Map.of("status", "sent", "type", "POST: Compra pendiente", "purchaseId", purchaseId));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("ok", false, "message", ex.getMessage()));
        }
    }

    // Enviar un evento de Compra pendiente de prueba usando valores por defecto (sin body)
    @GetMapping("/send-purchase-pending/simple")
    public ResponseEntity<?> sendPurchasePendingSimple() {
        try {
            int purchaseId = 999;
            Map<String, Object> user = Map.of("id", 999L, "name", "Simple Test", "email", "simple@example.com");
            Map<String, Object> cart = Map.of("cartId", 999L, "finalPrice", 1.0f, "cartItems", new java.util.ArrayList<>());
            ecommerceEventService.emitPurchasePending(purchaseId, user, cart);
            return ResponseEntity.ok(Map.of("status", "sent", "type", "POST: Compra pendiente", "purchaseId", purchaseId));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("ok", false, "message", ex.getMessage()));
        }
    }
}
