package ar.edu.uade.ecommerce.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

// Se añade un nombre de bean explícito para evitar el conflicto con
// ar.edu.uade.ecommerce.config.RestTemplateConfig (mismo nombre de clase simple).
@Configuration("messagingRestTemplateConfig")
public class RestTemplateConfig {

    private final BearerTokenInterceptor bearerTokenInterceptor;

    @Autowired
    public RestTemplateConfig(BearerTokenInterceptor bearerTokenInterceptor) {
        this.bearerTokenInterceptor = bearerTokenInterceptor;
    }

    @Bean
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Evitamos usar las APIs marcadas para eliminación en RestTemplateBuilder
        RestTemplate rt = builder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
                    factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
                    return factory;
                })
                .build();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(rt.getInterceptors());
        interceptors.add(bearerTokenInterceptor);
        rt.setInterceptors(interceptors);
        return rt;
    }
}
