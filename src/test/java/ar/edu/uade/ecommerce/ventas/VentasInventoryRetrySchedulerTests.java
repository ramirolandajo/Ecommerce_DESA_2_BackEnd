package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Entity.ConsumedEventLog;
import ar.edu.uade.ecommerce.Entity.ConsumedEventStatus;
import ar.edu.uade.ecommerce.Repository.ConsumedEventLogRepository;
import ar.edu.uade.ecommerce.messaging.CoreAckClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VentasInventoryRetrySchedulerTests {

    ConsumedEventLogRepository repo;
    VentasInventoryEventDispatcher dispatcher;
    ObjectMapper mapper;
    CoreAckClient ackClient;
    VentasInventoryRetryScheduler scheduler;

    @BeforeEach
    void setup() {
        repo = mock(ConsumedEventLogRepository.class);
        dispatcher = mock(VentasInventoryEventDispatcher.class);
        mapper = new ObjectMapper();
        ackClient = mock(CoreAckClient.class);
        scheduler = new VentasInventoryRetryScheduler(repo, dispatcher, mapper, ackClient);
        ReflectionTestUtils.setField(scheduler, "maxAttempts", 5);
        ReflectionTestUtils.setField(scheduler, "cooldownMinutes", 0); // procesar todo
        ReflectionTestUtils.setField(scheduler, "batchSize", 100);
    }

    @Test
    void runBatch_reprocesses_and_updates_log_and_ack() {
        ConsumedEventLog e = new ConsumedEventLog();
        e.setEventId("r1");
        e.setEventType("POST: Producto creado");
        e.setPayloadJson("{\"a\":1}");
        e.setStatus(ConsumedEventStatus.ERROR);
        e.setAttempts(1);
        e.setUpdatedAt(OffsetDateTime.now().minusMinutes(10));

        when(repo.findByStatusInAndAttemptsLessThanAndUpdatedAtBeforeOrderByUpdatedAtAsc(any(), anyInt(), any(), any()))
                .thenReturn(List.of(e));
        when(ackClient.sendAck(eq("r1"), anyString())).thenReturn(true);

        scheduler.runBatch();

        assertEquals(ConsumedEventStatus.PROCESSED, e.getStatus());
        assertNull(e.getLastError());
        assertTrue(e.getAttempts() >= 2);
        assertEquals(Boolean.TRUE, e.getAckSent());
        verify(repo, atLeast(1)).save(any());
        verify(dispatcher).process(any());
    }

    @Test
    void runBatch_when_dispatcher_throws_marks_error() {
        ConsumedEventLog e = new ConsumedEventLog();
        e.setEventId("r2");
        e.setEventType("POST: Producto creado");
        e.setPayloadJson("{\"a\":1}");
        e.setStatus(ConsumedEventStatus.ERROR);
        e.setAttempts(1);
        e.setUpdatedAt(OffsetDateTime.now().minusMinutes(10));

        when(repo.findByStatusInAndAttemptsLessThanAndUpdatedAtBeforeOrderByUpdatedAtAsc(any(), anyInt(), any(), any()))
                .thenReturn(List.of(e));
        doThrow(new RuntimeException("err")).when(dispatcher).process(any());

        scheduler.runBatch();

        assertEquals(ConsumedEventStatus.ERROR, e.getStatus());
        assertNotNull(e.getLastError());
        verify(repo, atLeast(1)).save(any());
    }
}
