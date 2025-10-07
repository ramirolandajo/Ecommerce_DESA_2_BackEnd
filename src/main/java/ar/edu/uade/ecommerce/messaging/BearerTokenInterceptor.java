package ar.edu.uade.ecommerce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${keycloak.token.required:true}")
    private boolean tokenRequired;

    @Autowired
    public BearerTokenInterceptor(BackendTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        String token = null;
        try {
            token = tokenManager.getToken();
            if (token != null && !token.isBlank()) {
                HttpHeaders headers = request.getHeaders();
                headers.setBearerAuth(token);
                logger.debug("BearerTokenInterceptor añadió Authorization header");
            } else {
                if (tokenRequired) {
                    String msg = "BearerTokenInterceptor: token requerido no disponible (Keycloak). Abortando request.";
                    logger.warn(msg);
                    throw new IOException(msg);
                } else {
                    logger.debug("BearerTokenInterceptor: no hay token disponible y no es requerido; se envía sin Authorization");
                }
            }
        } catch (IOException ioe) {
            throw ioe; // propagar corte de flujo
        } catch (Exception ex) {
            if (tokenRequired) {
                String msg = "BearerTokenInterceptor: error obteniendo token y es requerido: " + ex.getMessage();
                logger.warn(msg);
                throw new IOException(msg, ex);
            } else {
                logger.debug("BearerTokenInterceptor: error obteniendo token, pero no es requerido: {}", ex.getMessage());
            }
        }
        return execution.execute(request, body);
    }
}
