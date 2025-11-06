package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CoreApiClientSuccessAdditionalTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void sendEvent_whenRestTemplateReturnsOk_shouldNotThrow() {
        // Construir con base URL y puerto distintos para no caer en early-return
        CoreApiClient coreApiClient = new CoreApiClient(restTemplate, "http://otherhost:8080", "8081");

        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        CoreEvent event = new CoreEvent("TYP", "payload", "test-module");

        assertDoesNotThrow(() -> coreApiClient.sendEvent(event));
        verify(restTemplate).postForEntity(contains("/events"), any(), eq(Void.class));
    }
}
