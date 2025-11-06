package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoreAckClientMethodCaseTests {

    @Test
    void methodCaseInsensitive_trimmedToUpper() {
        RestTemplate rt = mock(RestTemplate.class);
        when(rt.exchange(anyString(), eq(org.springframework.http.HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));
        CoreAckClient client = new CoreAckClient(rt, "http://host:8080", true, "/events/{eventId}/ack", " put ");
        assertTrue(client.sendAck("42", "mod"));
    }

    @Test
    void baseEndsWithSlash_buildsUrlCorrectly() {
        RestTemplate rt = mock(RestTemplate.class);
        when(rt.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(ResponseEntity.ok("OK"));
        CoreAckClient client = new CoreAckClient(rt, "http://host:8080/", true, "/events/{eventId}/ack", "POST");
        assertTrue(client.sendAck("2", "m"));
        verify(rt).postForEntity(eq("http://host:8080/events/2/ack"), any(HttpEntity.class), eq(String.class));
    }
}

