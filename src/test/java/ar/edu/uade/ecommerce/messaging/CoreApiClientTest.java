package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CoreApiClientTest {
    @Test
    void whenCommunicationUrlNotConfigured_sendEventDoesNothing() {
        RestTemplate rt = mock(RestTemplate.class);
        CoreApiClient client = new CoreApiClient(rt, "", "8080");
        // Should not throw
        client.sendEvent(new CoreEvent("T", null, "M"));
        verifyNoInteractions(rt);
    }

    @Test
    void whenCommunicationUrlPointsToSamePort_doNotSend() {
        RestTemplate rt = mock(RestTemplate.class);
        String base = "http://localhost:8081/comm";
        CoreApiClient client = new CoreApiClient(rt, base, "8081");
        client.sendEvent(new CoreEvent("TYP", null, "M"));
        verifyNoInteractions(rt);
    }

    @Test
    void whenRestTemplateThrows_sendEventCatches() {
        RestTemplate rt = mock(RestTemplate.class);
        String base = "http://otherhost:8080";
        CoreApiClient client = new CoreApiClient(rt, base, "8081");
        when(rt.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class))).thenThrow(new RuntimeException("boom"));
        // Should not propagate
        client.sendEvent(new CoreEvent("TYP", null, "M"));
        verify(rt, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Void.class));
    }
}

