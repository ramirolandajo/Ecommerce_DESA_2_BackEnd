package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

class CoreApiClientNullEventTests {

    @Test
    void sendEvent_withNullEvent_stillPosts() {
        RestTemplate rt = mock(RestTemplate.class);
        CoreApiClient client = new CoreApiClient(rt, "http://host:8080", "8081");
        client.sendEvent(null);
        verify(rt).postForEntity(contains("/events"), any(HttpEntity.class), eq(Void.class));
    }
}
