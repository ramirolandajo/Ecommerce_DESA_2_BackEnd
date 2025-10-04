package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Entity.ConsumedEventLog;
import ar.edu.uade.ecommerce.Entity.ConsumedEventStatus;
import ar.edu.uade.ecommerce.Repository.ConsumedEventLogRepository;
import ar.edu.uade.ecommerce.messaging.CoreAckClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "ventas.retry", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VentasInventoryRetryScheduler {
    private static final Logger log = LoggerFactory.getLogger(VentasInventoryRetryScheduler.class);

    private final ConsumedEventLogRepository repo;
    private final VentasInventoryEventDispatcher dispatcher;
    private final ObjectMapper mapper;
    private final CoreAckClient ackClient;

    @Value("${ventas.retry.maxAttempts:5}")
    private int maxAttempts;

    @Value("${ventas.retry.cooldown.minutes:30}")
    private int cooldownMinutes;

    @Value("${ventas.retry.batchSize:100}")
    private int batchSize;

    public VentasInventoryRetryScheduler(ConsumedEventLogRepository repo,
                                         VentasInventoryEventDispatcher dispatcher,
                                         ObjectMapper mapper,
                                         CoreAckClient ackClient) {
        this.repo = repo;
        this.dispatcher = dispatcher;
        this.mapper = mapper;
        this.ackClient = ackClient;
    }

    // Corre cada 6 horas por defecto. Cron configurable por propiedad ventas.retry.cron
    @Scheduled(cron = "${ventas.retry.cron:0 0 */6 * * *}")
    public void runBatch() {
        OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(cooldownMinutes);
        List<ConsumedEventLog> pending = repo.findByStatusInAndAttemptsLessThanAndUpdatedAtBeforeOrderByUpdatedAtAsc(
                List.of(ConsumedEventStatus.ERROR, ConsumedEventStatus.PENDING),
                maxAttempts,
                threshold,
                PageRequest.of(0, batchSize)
        );
        if (pending.isEmpty()) {
            log.debug("[RetryScheduler] No hay eventos pendientes para reprocesar.");
            return;
        }
        log.info("[RetryScheduler] Reintentando {} eventos (cooldown={}m, maxAttempts={})", pending.size(), cooldownMinutes, maxAttempts);
        for (ConsumedEventLog e : pending) {
            try {
                EventMessage msg = toEventMessage(e);
                dispatcher.process(msg);
                e.setStatus(ConsumedEventStatus.PROCESSED);
                e.setLastError(null);
                e.setAttempts((e.getAttempts() == null ? 0 : e.getAttempts()) + 1);

                // Intentar enviar ACK si aún no se envió
                boolean ackOk = ackClient.sendAck(e.getEventId(), "ventas");
                e.setAckAttempts((e.getAckAttempts() == null ? 0 : e.getAckAttempts()) + 1);
                e.setAckLastAt(OffsetDateTime.now());
                e.setAckSent(ackOk);
                if (!ackOk) e.setAckLastError("ACK failed (ver logs)\n" + (e.getAckLastError() != null ? e.getAckLastError() : ""));
                else e.setAckLastError(null);
            } catch (Exception ex) {
                e.setStatus(ConsumedEventStatus.ERROR);
                e.setLastError(ex.toString());
                e.setAttempts((e.getAttempts() == null ? 0 : e.getAttempts()) + 1);
                log.warn("[RetryScheduler] Error reprocesando eventId={} type={} - {}", e.getEventId(), e.getEventType(), ex.getMessage());
            } finally {
                e.setUpdatedAt(OffsetDateTime.now());
                repo.save(e);
            }
        }
    }

    private EventMessage toEventMessage(ConsumedEventLog e) throws Exception {
        EventMessage msg = new EventMessage();
        msg.setEventId(e.getEventId());
        msg.setEventType(e.getEventType());
        msg.setOriginModule(e.getOriginModule());
        msg.setTimestamp(e.getTimestampRaw());
        if (e.getPayloadJson() != null) {
            JsonNode node = mapper.readTree(e.getPayloadJson());
            msg.setPayload(node);
        }
        return msg;
    }
}
