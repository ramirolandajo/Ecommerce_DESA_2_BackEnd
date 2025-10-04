package ar.edu.uade.ecommerce.ventas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "ventas.kafka", name = "listen-ventas", havingValue = "true", matchIfMissing = false)
public class VentasEventsListener {

    private static final Logger log = LoggerFactory.getLogger(VentasEventsListener.class);

    private final EventIdempotencyService idempotencyService;
    private final VentasConsumerMonitorService monitor;

    public VentasEventsListener(EventIdempotencyService idempotencyService, VentasConsumerMonitorService monitor) {
        this.idempotencyService = idempotencyService;
        this.monitor = monitor;
    }

    @KafkaListener(
            topics = "${ventas.kafka.topic:ventas.events}",
            containerFactory = "ventasKafkaListenerContainerFactory"
    )
    public void onMessage(@Payload EventMessage msg) {
        if (msg == null) {
            log.warn("[Ventas][Kafka] Mensaje nulo recibido. Ignorado.");
            return;
        }
        String eventId = msg.getEventId();
        String eventType = msg.getEventType();
        String t = msg.getNormalizedEventType();

        if (idempotencyService.alreadyProcessed(eventId)) {
            log.info("[Ventas][Kafka] Evento ya procesado. eventId={} eventType={}", eventId, eventType);
            monitor.recordDuplicate(eventId);
            return;
        }

        log.info("[Ventas][Kafka] Recibido eventId={} type='{}' origin={} ts={}",
                eventId, eventType, msg.getOriginModule(), msg.getTimestamp());

        try {
            dispatch(t, msg);
            idempotencyService.markProcessed(eventId);
            monitor.recordProcessed(eventType, eventId);
            log.info("[Ventas][Kafka] Procesado OK eventId={} type={}", eventId, eventType);
        } catch (Exception ex) {
            monitor.recordError(eventType, eventId);
            log.error("[Ventas][Kafka] Error procesando eventId={} type={} - {}", eventId, eventType, ex.getMessage(), ex);
            // No marcamos como procesado para permitir reintentos
            throw ex; // dejar que el contenedor maneje el retry/backoff
        }
    }

    private void dispatch(String normalizedType, EventMessage msg) {
        if (normalizedType == null) {
            log.warn("[Ventas][Kafka] eventType nulo, se ignora. msg={}", msg);
            return;
        }
        switch (normalizedType) {
            case "post: compra confirmada" -> handleCompraConfirmada(msg);
            case "delete: compra cancelada" -> handleCompraCancelada(msg);
            case "post: compra pendiente" -> handleCompraPendiente(msg);
            case "post: review creada" -> handleReviewCreada(msg);
            case "post: producto agregado a favoritos" -> handleFavoritoAgregado(msg);
            case "delete: producto quitado de favoritos" -> handleFavoritoQuitado(msg);
            default -> log.info("[Ventas][Kafka] Evento no reconocido, ignorado: {}", msg.getEventType());
        }
    }

    private void handleCompraConfirmada(EventMessage msg) {
        log.info("[Ventas][Handler] Compra confirmada payload={}", msg.getPayload());
        // TODO: lógica de persistencia/acciones
    }

    private void handleCompraCancelada(EventMessage msg) {
        log.info("[Ventas][Handler] Compra cancelada payload={}", msg.getPayload());
        // TODO: lógica de persistencia/acciones
    }

    private void handleCompraPendiente(EventMessage msg) {
        log.info("[Ventas][Handler] Compra pendiente payload={}", msg.getPayload());
        // TODO: lógica de persistencia/acciones
    }

    private void handleReviewCreada(EventMessage msg) {
        log.info("[Ventas][Handler] Review creada payload={}", msg.getPayload());
        // TODO: lógica de persistencia/acciones
    }

    private void handleFavoritoAgregado(EventMessage msg) {
        log.info("[Ventas][Handler] Producto agregado a favoritos payload={}", msg.getPayload());
        // TODO: lógica de persistencia/acciones
    }

    private void handleFavoritoQuitado(EventMessage msg) {
        log.info("[Ventas][Handler] Producto quitado de favoritos payload={}", msg.getPayload());
        // TODO: lógica de persistencia/acciones
    }
}
