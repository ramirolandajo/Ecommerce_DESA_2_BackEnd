package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

class CoreApiClientMoreTests {

    @Test
    void sendEvent_whenBaseNull_doesNotCallRestTemplate() {
        RestTemplate rt = mock(RestTemplate.class);
        CoreApiClient client = new CoreApiClient(rt, null, "8081");
        client.sendEvent(new CoreEvent("TYP", "{}", "mod"));
        verifyNoInteractions(rt);
    }

    @Test
    void sendEvent_whenSamePort_omitsSending() {
        RestTemplate rt = mock(RestTemplate.class);
        CoreApiClient client = new CoreApiClient(rt, "http://localhost:8081/comm", "8081");
        client.sendEvent(new CoreEvent("TYP", "{}", "mod"));
        verifyNoInteractions(rt);
    }

    @Test
    void sendEvent_whenOk_callsPostForEntity() {
        RestTemplate rt = mock(RestTemplate.class);
        when(rt.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class))).thenReturn(ResponseEntity.ok().build());
        CoreApiClient client = new CoreApiClient(rt, "http://otherhost:8080", "8081");
        client.sendEvent(new CoreEvent("TYP", "{}", "mod"));
        verify(rt).postForEntity(ArgumentMatchers.contains("/events"), any(HttpEntity.class), eq(Void.class));
    }
}

