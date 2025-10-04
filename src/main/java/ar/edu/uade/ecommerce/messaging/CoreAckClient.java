package ar.edu.uade.ecommerce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CoreAckClient {
    private static final Logger log = LoggerFactory.getLogger(CoreAckClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final boolean ackEnabled;
    private final String ackPath; // e.g. /events/{eventId}/ack
    private final String ackMethod; // POST o PUT

    public CoreAckClient(RestTemplate restTemplate,
                         @Value("${communication.intermediary.url}") String baseUrl,
                         @Value("${communication.intermediary.ack.enabled:true}") boolean ackEnabled,
                         @Value("${communication.intermediary.ack.path:/events/{eventId}/ack}") String ackPath,
                         @Value("${communication.intermediary.ack.method:POST}") String ackMethod) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.ackEnabled = ackEnabled;
        this.ackPath = ackPath;
        this.ackMethod = ackMethod == null ? "POST" : ackMethod.trim().toUpperCase();
        log.info("CoreAckClient inicializado (enabled={} path='{}' method='{}')", ackEnabled, ackPath, this.ackMethod);
    }

    public boolean sendAck(String eventId, String consumerModule) {
        if (!ackEnabled) {
            log.debug("ACK deshabilitado; no se enviará ack para eventId={}", eventId);
            return false;
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            log.warn("communication.intermediary.url no configurado; no se enviará ACK para eventId={}", eventId);
            return false;
        }
        if (eventId == null || eventId.isBlank()) {
            log.warn("eventId vacío; no se envía ACK");
            return false;
        }
        String path = ackPath.replace("{eventId}", eventId);
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) + path : baseUrl + path;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("eventId", eventId);
            body.put("consumer", consumerModule);
            body.put("status", "CONSUMED");
            body.put("consumedAt", OffsetDateTime.now().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp;
            if ("PUT".equals(ackMethod)) {
                resp = restTemplate.exchange(url, HttpMethod.PUT, req, String.class);
            } else {
                resp = restTemplate.postForEntity(url, req, String.class);
            }
            log.info("ACK enviado al middleware: eventId={} status={} httpStatus={}", eventId, body.get("status"), resp.getStatusCode());
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Error enviando ACK para eventId={}: {}", eventId, ex.getMessage());
            return false;
        }
    }
}

