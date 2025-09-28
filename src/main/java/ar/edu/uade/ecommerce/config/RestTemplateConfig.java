package ar.edu.uade.ecommerce.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean("fallbackRestTemplate")
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate fallbackRestTemplate() {
        return new RestTemplate();
    }
}
