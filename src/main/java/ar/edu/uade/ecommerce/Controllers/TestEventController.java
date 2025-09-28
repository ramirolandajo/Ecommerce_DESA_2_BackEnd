package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestEventController {
    @Autowired
    private ECommerceEventService ecommerceEventService;

    @PostMapping("/send-event")
    public ResponseEntity<?> sendEvent(@RequestBody Map<String, Object> body) {
        String type = (String) body.getOrDefault("type", "TEST_EVENT");
        Object payload = body.getOrDefault("payload", Map.of("msg", "hello"));
        ecommerceEventService.emitRawEvent(type, payload);
        return ResponseEntity.ok(Map.of("status", "sent", "type", type));
    }
}

