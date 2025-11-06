package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoreAckClientNon2xxTests {

    @Test
    void sendAck_whenServerReturns500_returnsFalse() {
        RestTemplate rt = mock(RestTemplate.class);
        when(rt.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.status(500).body("X"));
        CoreAckClient client = new CoreAckClient(rt, "http://host:8080", true, "/events/{eventId}/ack", "POST");
        assertFalse(client.sendAck("1", "m"));
    }
}

