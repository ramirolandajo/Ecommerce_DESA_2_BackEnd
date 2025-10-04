package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Entity.ConsumedEventLog;
import ar.edu.uade.ecommerce.Entity.ConsumedEventStatus;
import ar.edu.uade.ecommerce.Repository.ConsumedEventLogRepository;
import ar.edu.uade.ecommerce.messaging.CoreAckClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@ConditionalOnProperty(prefix = "inventario.kafka", name = "listen-inventario", havingValue = "true", matchIfMissing = true)
public class InventarioEventsListener {
    private static final Logger log = LoggerFactory.getLogger(InventarioEventsListener.class);

    private final EventIdempotencyService idempotencyService;
    private final VentasConsumerMonitorService monitor;
    private final VentasInventoryEventDispatcher dispatcher;
    private final ConsumedEventLogRepository eventLogRepo;
    private final ObjectMapper mapper;
    private final CoreAckClient ackClient;

    public InventarioEventsListener(EventIdempotencyService idempotencyService,
                                    VentasConsumerMonitorService monitor,
                                    VentasInventoryEventDispatcher dispatcher,
                                    ConsumedEventLogRepository eventLogRepo,
                                    ObjectMapper mapper,
                                    CoreAckClient ackClient) {
        this.idempotencyService = idempotencyService;
        this.monitor = monitor;
        this.dispatcher = dispatcher;
        this.eventLogRepo = eventLogRepo;
        this.mapper = mapper;
        this.ackClient = ackClient;
    }

    @KafkaListener(
            topics = "${inventario.kafka.topic:inventario}",
            containerFactory = "ventasKafkaListenerContainerFactory",
            concurrency = "${inventario.kafka.concurrency:1}"
    )
    public void onMessage(
            @Payload EventMessage msg,
            @Header(name = KafkaHeaders.RECEIVED_TOPIC, required = false) String topic,
            @Header(name = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(name = KafkaHeaders.OFFSET, required = false) Long offset
    ) {
        if (msg == null) {
            log.warn("[Inventario->Ventas][Kafka] Mensaje nulo recibido. Ignorado. topic={} partition={} offset={}", topic, partition, offset);
            return;
        }
        String eventId = msg.getEventId();
        String eventType = msg.getEventType();
        log.info("[Inventario->Ventas][Kafka] Recibido eventId={} type='{}' origin={} ts={} topic={} partition={} offset={}",
                eventId, eventType, msg.getOriginModule(), msg.getTimestamp(), topic, partition, offset);

        // Registrar/actualizar log del evento
        ConsumedEventLog logRow = eventLogRepo.findByEventId(eventId).orElseGet(ConsumedEventLog::new);
        logRow.setEventId(eventId);
        logRow.setEventType(eventType);
        logRow.setOriginModule(msg.getOriginModule());
        logRow.setTimestampRaw(msg.getTimestamp() != null ? msg.getTimestamp().toString() : null);
        logRow.setTopic(topic);
        logRow.setPartitionId(partition);
        logRow.setOffsetValue(offset);
        try {
            String payloadJson = mapper.writeValueAsString(msg.getPayload());
            logRow.setPayloadJson(payloadJson);
        } catch (Exception ignore) {
            // En caso de no poder serializar, lo dejamos nulo
        }
        if (logRow.getStatus() == null || logRow.getStatus() == ConsumedEventStatus.ERROR) {
            logRow.setStatus(ConsumedEventStatus.PENDING);
        }
        logRow.setUpdatedAt(OffsetDateTime.now());
        logRow = eventLogRepo.save(logRow);

        // Idempotencia: si ya lo procesamos antes (memoria o DB), no reprocesar
        if (idempotencyService.alreadyProcessed(eventId) || logRow.getStatus() == ConsumedEventStatus.PROCESSED) {
            log.info("[Inventario->Ventas][Kafka] Evento ya procesado. eventId={} eventType={} topic={} partition={} offset={}",
                    eventId, eventType, topic, partition, offset);
            // Si no se envió ACK, intentarlo ahora
            if (Boolean.FALSE.equals(logRow.getAckSent())) {
                boolean ackOk = ackClient.sendAck(eventId, "ventas");
                logRow.setAckAttempts((logRow.getAckAttempts() == null ? 0 : logRow.getAckAttempts()) + 1);
                logRow.setAckLastAt(OffsetDateTime.now());
                logRow.setAckSent(ackOk);
                if (!ackOk) logRow.setAckLastError("ACK failed (ver logs)"); else logRow.setAckLastError(null);
                eventLogRepo.save(logRow);
                log.info("[Inventario->Ventas][Kafka] ACK post-procesado eventId={} ackSent={}", eventId, ackOk);
            }
            monitor.recordDuplicate(eventId);
            return;
        }

        try {
            dispatcher.process(msg);
            idempotencyService.markProcessed(eventId);
            logRow.setStatus(ConsumedEventStatus.PROCESSED);
            logRow.setAttempts(logRow.getAttempts() == null ? 1 : logRow.getAttempts() + 1);
            logRow.setLastError(null);
            logRow.setUpdatedAt(OffsetDateTime.now());

            // Intentar enviar ACK al middleware
            boolean ackOk = ackClient.sendAck(eventId, "ventas");
            logRow.setAckAttempts((logRow.getAckAttempts() == null ? 0 : logRow.getAckAttempts()) + 1);
            logRow.setAckLastAt(OffsetDateTime.now());
            logRow.setAckSent(ackOk);
            if (!ackOk) {
                logRow.setAckLastError("ACK failed (ver logs)");
            } else {
                logRow.setAckLastError(null);
            }

            eventLogRepo.save(logRow);
            monitor.recordProcessed(eventType, eventId);
            log.info("[Inventario->Ventas][Kafka] Procesado OK eventId={} type={} topic={} partition={} offset={} ackSent={}",
                    eventId, eventType, topic, partition, offset, ackOk);
        } catch (Exception ex) {
            monitor.recordError(eventType, eventId);
            log.error("[Inventario->Ventas][Kafka] Error procesando eventId={} type={} topic={} partition={} offset={} - {}",
                    eventId, eventType, topic, partition, offset, ex.getMessage(), ex);
            logRow.setStatus(ConsumedEventStatus.ERROR);
            logRow.setAttempts(logRow.getAttempts() == null ? 1 : logRow.getAttempts() + 1);
            logRow.setLastError(ex.toString());
            logRow.setUpdatedAt(OffsetDateTime.now());
            eventLogRepo.save(logRow);
            throw ex; // permitir reintentos/backoff del contenedor
        }
    }
}
