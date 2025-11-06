package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestTemplateConfigTests {

    @Test
    void restTemplate_addsBearerTokenInterceptor() {
        BearerTokenInterceptor interceptor = mock(BearerTokenInterceptor.class);
        RestTemplateConfig cfg = new RestTemplateConfig(interceptor);
        RestTemplate rt = cfg.restTemplate(new RestTemplateBuilder());
        List<ClientHttpRequestInterceptor> ints = rt.getInterceptors();
        assertTrue(ints.stream().anyMatch(i -> i == interceptor));
    }
}

