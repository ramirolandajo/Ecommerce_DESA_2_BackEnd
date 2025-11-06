package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Entity.ConsumedEventLog;
import ar.edu.uade.ecommerce.Entity.ConsumedEventStatus;
import ar.edu.uade.ecommerce.Repository.ConsumedEventLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VentasConsumersHealthControllerTests {

    @Test
    void status_returns_counts_and_last() {
        ConsumedEventLogRepository repo = mock(ConsumedEventLogRepository.class);
        VentasConsumersHealthController c = new VentasConsumersHealthController(repo);

        when(repo.countByStatus(ConsumedEventStatus.PROCESSED)).thenReturn(5L);
        when(repo.countByStatus(ConsumedEventStatus.PENDING)).thenReturn(2L);
        when(repo.countByStatus(ConsumedEventStatus.ERROR)).thenReturn(1L);

        ConsumedEventLog e = new ConsumedEventLog();
        e.setEventId("x");
        e.setEventType("t");
        e.setStatus(ConsumedEventStatus.PROCESSED);
        e.setAttempts(2);
        e.setUpdatedAt(OffsetDateTime.now());
        e.setTopic("inventario");
        e.setPartitionId(0);
        e.setOffsetValue(10L);

        when(repo.findAllByOrderByUpdatedAtDesc(any(PageRequest.class))).thenReturn(List.of(e));

        Map<String, Object> out = c.status(5);

        assertEquals(5L, out.get("processed"));
        assertEquals(2L, out.get("pending"));
        assertEquals(1L, out.get("error"));
        List<?> last = (List<?>) out.get("last");
        assertEquals(1, last.size());
    }
}
