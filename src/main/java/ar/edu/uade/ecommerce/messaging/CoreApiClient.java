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
    private final String coreBaseUrl;

    // Nueva propiedad: URL del intermediario de comunicación (CommunicationCoreIntermediate)
    private final String communicationBaseUrl;

    // Leer el puerto del servidor para detectar llamadas a sí mismo
    private final String serverPort;

    // Usamos un RestTemplate interno para evitar problemas de autowire en entornos donde no existe el bean
    public CoreApiClient(@Value("${core.api.url:http://core-api.local}") String coreBaseUrl,
                         @Value("${communication.intermediary.url:}") String communicationBaseUrl,
                         @Value("${server.port:8081}") String serverPort) {
        this.restTemplate = new RestTemplate();
        this.coreBaseUrl = coreBaseUrl;
        this.communicationBaseUrl = (communicationBaseUrl != null && !communicationBaseUrl.isBlank()) ? communicationBaseUrl : null;
        this.serverPort = serverPort;
        logger.info("CoreApiClient inicializado con coreBaseUrl='{}' communicationBaseUrl='{}' server.port='{}'", coreBaseUrl, communicationBaseUrl, serverPort);
    }

    public void sendEvent(CoreEvent event) {
        // Preferir la URL del intermediario si fue configurada, de lo contrario usar coreBaseUrl
        String base = (communicationBaseUrl != null) ? communicationBaseUrl : coreBaseUrl;
        String url = base.endsWith("/") ? base + "events" : base + "/events";
        try {
            // Evitar enviar eventos al mismo puerto de la app (posible loop en desarrollo)
            if ((base != null && base.contains(":" + serverPort)) || (base != null && base.contains("localhost:" + serverPort))) {
                logger.warn("Se ha detectado que la URL de destino apunta al mismo puerto que la aplicación ({}). Omitiendo envío de evento para evitar loop: {}", serverPort, event.type);
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Si el evento trae un token originModule, enviarlo también en Authorization header
            if (event != null && event.originModule != null && !event.originModule.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + event.originModule);
                logger.debug("Añadiendo Authorization header (token length={}) para evento {}", event.originModule.length(), event.type);
            } else {
                logger.debug("No se añadió Authorization header porque originModule está vacío o nulo para evento {}", event != null ? event.type : "<null>");
            }
            HttpEntity<CoreEvent> request = new HttpEntity<>(event, headers);
            ResponseEntity<Void> resp = restTemplate.postForEntity(url, request, Void.class);
            logger.info("Evento enviado a la API de Comunicación (destino='{}'): {} -> status={}", base, event != null ? event.type : "<null>", resp.getStatusCode());
        } catch (Exception ex) {
            logger.error("Error enviando evento a la API de Comunicación (destino='{}'): {}", base, ex.getMessage(), ex);
        }
    }
}
