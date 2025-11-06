package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

class CoreApiClientEmptyBaseTests {

    @Test
    void sendEvent_withEmptyBaseUrl_doesNotPost() {
        RestTemplate rt = mock(RestTemplate.class);
        CoreApiClient client = new CoreApiClient(rt, "", "8081");
        client.sendEvent(new CoreEvent("T", "{}", "m"));
        verifyNoInteractions(rt);
    }
}

