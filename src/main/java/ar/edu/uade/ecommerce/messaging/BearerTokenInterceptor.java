package ar.edu.uade.ecommerce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BearerTokenInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(BearerTokenInterceptor.class);

    private final BackendTokenManager tokenManager;

    @Autowired
    public BearerTokenInterceptor(BackendTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        try {
            String token = tokenManager.getToken();
            if (token != null && !token.isBlank()) {
                HttpHeaders headers = request.getHeaders();
                headers.setBearerAuth(token);
                logger.debug("BearerTokenInterceptor añadió Authorization header");
            } else {
                logger.debug("BearerTokenInterceptor: no hay token disponible, no se añade header");
            }
        } catch (Exception ex) {
            logger.warn("BearerTokenInterceptor: error obteniendo token: {}", ex.getMessage());
        }
        return execution.execute(request, body);
    }
}
