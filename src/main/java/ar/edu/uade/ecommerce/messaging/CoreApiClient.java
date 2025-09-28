package ar.edu.uade.ecommerce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CoreApiClient {
    private static final Logger logger = LoggerFactory.getLogger(CoreApiClient.class);
    private final RestTemplate restTemplate;

    // URL del intermediario de comunicación (CommunicationCoreIntermediate)
    private final String communicationBaseUrl;

    // Leer el puerto del servidor para detectar llamadas a sí mismo
    private final String serverPort;

    public CoreApiClient(RestTemplate restTemplate,
                         @Value("${communication.intermediary.url}") String communicationBaseUrl,
                         @Value("${server.port:8081}") String serverPort) {
        this.restTemplate = restTemplate; // Usa el bean con BearerTokenInterceptor
        this.communicationBaseUrl = (communicationBaseUrl != null && !communicationBaseUrl.isBlank()) ? communicationBaseUrl : null;
        this.serverPort = serverPort;
        logger.info("CoreApiClient inicializado con communicationBaseUrl='{}' server.port='{}'", communicationBaseUrl, serverPort);
    }

    public void sendEvent(CoreEvent event) {
        String base = communicationBaseUrl;
        if (base == null) {
            logger.warn("communication.intermediary.url no configurado; no se enviará el evento '{}'.", event != null ? event.type : "<null>");
            return;
        }
        String url = base.endsWith("/") ? base + "events" : base + "/events";
        try {
            // Evitar enviar eventos al mismo puerto de la app (posible loop en desarrollo)
            if ((base.contains(":" + serverPort)) || (base.contains("localhost:" + serverPort))) {
                logger.warn("Destino apunta al mismo puerto que la aplicación ({}). Omitiendo envío de evento: {}", serverPort, event.type);
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Nota: El Authorization header lo agrega BearerTokenInterceptor con el token de Keycloak del cliente
            HttpEntity<CoreEvent> request = new HttpEntity<>(event, headers);
            ResponseEntity<Void> resp = restTemplate.postForEntity(url, request, Void.class);
            logger.info("Evento enviado a la API de Comunicación (destino='{}'): {} -> status={}", base, event != null ? event.type : "<null>", resp.getStatusCode());
        } catch (Exception ex) {
            logger.error("Error enviando evento a la API de Comunicación (destino='{}'): {}", base, ex.getMessage(), ex);
        }
    }
}
