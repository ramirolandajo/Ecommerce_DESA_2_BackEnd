package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.messaging.ECommerceEventService;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestEventController {
    @Autowired
    private ECommerceEventService ecommerceEventService;

    @Value("${communication.intermediary.url}")
    private String middlewareURI;

    @PostMapping("/send-event")
    public ResponseEntity<?> sendEvent(@RequestBody Map<String, Object> body) {
        String type = (String) body.getOrDefault("type", "TEST_EVENT");
        Object payload = body.getOrDefault("payload", Map.of("msg", "hello"));
        ecommerceEventService.emitRawEvent(type, payload);
        return ResponseEntity.ok(Map.of("status", "sent", "type", type));
    }

    @GetMapping("/pingToMiddleware")
    public ResponseEntity<?>pingToMiddleware() throws IOException, InterruptedException{
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request =
         HttpRequest.newBuilder().uri(URI.create(middlewareURI + "api/hello"))
                    .header("Accept", "application/json")
                    .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("response: " + response);
        return ResponseEntity.ok("response.body: " + response.body());
        
    }
}

