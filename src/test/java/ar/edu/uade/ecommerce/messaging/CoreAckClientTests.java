package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoreAckClientTests {

    @Test
    void sendAck_returnsFalseWhenDisabled() {
        RestTemplate rt = mock(RestTemplate.class);
        CoreAckClient client = new CoreAckClient(rt, "http://host:8080", false, "/events/{eventId}/ack", "POST");
        assertFalse(client.sendAck("1", "mod"));
        verifyNoInteractions(rt);
    }

    @Test
    void sendAck_returnsFalseWhenBaseUrlMissing() {
        RestTemplate rt = mock(RestTemplate.class);
        CoreAckClient client = new CoreAckClient(rt, null, true, "/events/{eventId}/ack", "POST");
        assertFalse(client.sendAck("1", "mod"));
        verifyNoInteractions(rt);
    }

    @Test
    void sendAck_post_successReturnsTrue() {
        RestTemplate rt = mock(RestTemplate.class);
        when(rt.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));
        CoreAckClient client = new CoreAckClient(rt, "http://host:8080", true, "/events/{eventId}/ack", "POST");
        assertTrue(client.sendAck("10", "svc"));
        verify(rt).postForEntity(contains("/events/10/ack"), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void sendAck_put_successReturnsTrue() {
        RestTemplate rt = mock(RestTemplate.class);
        when(rt.exchange(anyString(), eq(org.springframework.http.HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));
        CoreAckClient client = new CoreAckClient(rt, "http://host:8080", true, "/events/{eventId}/ack", "PUT");
        assertTrue(client.sendAck("20", "svc"));
        verify(rt).exchange(contains("/events/20/ack"), eq(org.springframework.http.HttpMethod.PUT), any(HttpEntity.class), eq(String.class));
    }
}

