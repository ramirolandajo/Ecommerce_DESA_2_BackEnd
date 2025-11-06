package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoreAckClientEdgeTests {

    @Test
    void sendAck_blankEventId_returnsFalse() {
        RestTemplate rt = mock(RestTemplate.class);
        CoreAckClient client = new CoreAckClient(rt, "http://host:8080", true, "/events/{eventId}/ack", "POST");
        assertFalse(client.sendAck(" ", "m"));
        verifyNoInteractions(rt);
    }

    @Test
    void sendAck_restTemplateThrows_returnsFalse() {
        RestTemplate rt = mock(RestTemplate.class);
        when(rt.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("boom"));
        CoreAckClient client = new CoreAckClient(rt, "http://host:8080", true, "/events/{eventId}/ack", "POST");
        assertFalse(client.sendAck("1", "m"));
    }

    @Test
    void sendAck_methodNormalization_defaultsToPOST() {
        RestTemplate rt = mock(RestTemplate.class);
        when(rt.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));
        // method null => POST
        CoreAckClient client = new CoreAckClient(rt, "http://host:8080", true, "/events/{eventId}/ack", null);
        assertTrue(client.sendAck("2", "m"));
    }
}

