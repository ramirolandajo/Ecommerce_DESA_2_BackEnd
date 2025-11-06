package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class CoreApiClientErrorTests {

    @Test
    void sendEvent_handlesExceptionFromRestTemplate() {
        RestTemplate rt = mock(RestTemplate.class);
        when(rt.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new RuntimeException("boom"));
        CoreApiClient client = new CoreApiClient(rt, "http://host:8080", "8081");
        assertDoesNotThrow(() -> client.sendEvent(new CoreEvent("T", "{}", "mod")));
    }
}

