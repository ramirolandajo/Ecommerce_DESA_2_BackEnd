package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Entity.ConsumedEventLog;
import ar.edu.uade.ecommerce.Entity.ConsumedEventStatus;
import ar.edu.uade.ecommerce.Repository.ConsumedEventLogRepository;
import ar.edu.uade.ecommerce.messaging.CoreAckClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InventarioEventsListenerTests {

    EventIdempotencyService idem;
    VentasConsumerMonitorService monitor;
    VentasInventoryEventDispatcher dispatcher;
    ConsumedEventLogRepository repo;
    ObjectMapper mapper;
    CoreAckClient ackClient;
    InventarioEventsListener listener;

    @BeforeEach
    void init() {
        idem = mock(EventIdempotencyService.class);
        monitor = mock(VentasConsumerMonitorService.class);
        dispatcher = mock(VentasInventoryEventDispatcher.class);
        repo = mock(ConsumedEventLogRepository.class);
        mapper = new ObjectMapper();
        ackClient = mock(CoreAckClient.class);
        listener = new InventarioEventsListener(idem, monitor, dispatcher, repo, mapper, ackClient);
    }

    @Test
    void onMessage_persists_log_and_dispatches_and_acks() {
        EventMessage msg = new EventMessage();
        msg.setEventId("e1");
        msg.setEventType("POST: Producto creado");
        msg.setOriginModule("inventario");
        msg.setTimestamp(OffsetDateTime.now().toString());
        msg.setPayload(java.util.Map.of("x", 1));

        when(repo.findByEventId("e1")).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ackClient.sendAck(eq("e1"), anyString())).thenReturn(true);
        when(idem.alreadyProcessed("e1")).thenReturn(false);

        listener.onMessage(msg, "inventario", 0, 10L);

        verify(dispatcher).process(msg);
        verify(idem).markProcessed("e1");
        verify(ackClient).sendAck("e1", "ventas");
        verify(monitor).recordProcessed("POST: Producto creado", "e1");
        verify(repo, atLeast(1)).save(any());
    }

    @Test
    void onMessage_duplicate_sends_ack_if_not_sent_and_skips() {
        EventMessage msg = new EventMessage();
        msg.setEventId("e2");
        msg.setEventType("POST: Producto creado");

        ConsumedEventLog existing = new ConsumedEventLog();
        existing.setEventId("e2");
        existing.setStatus(ConsumedEventStatus.PROCESSED);
        existing.setAckSent(false);

        when(repo.findByEventId("e2")).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(idem.alreadyProcessed("e2")).thenReturn(true);
        when(ackClient.sendAck(eq("e2"), anyString())).thenReturn(true);

        listener.onMessage(msg, "inventario", 1, 11L);

        verify(dispatcher, never()).process(any());
        verify(ackClient).sendAck("e2", "ventas");
        verify(monitor).recordDuplicate("e2");
    }

    @Test
    void onMessage_error_marks_log_error_and_rethrows() {
        EventMessage msg = new EventMessage();
        msg.setEventId("e3");
        msg.setEventType("POST: Producto creado");

        when(repo.findByEventId("e3")).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(idem.alreadyProcessed("e3")).thenReturn(false);
        doThrow(new RuntimeException("boom")).when(dispatcher).process(any());

        assertThrows(RuntimeException.class, () -> listener.onMessage(msg, "inventario", 1, 12L));
        verify(monitor).recordError(eq("POST: Producto creado"), eq("e3"));
        verify(repo, atLeast(1)).save(any());
    }
}

