package ar.edu.uade.ecommerce.ventas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VentasConsumerMonitorServiceTests {

    @Test
    void records_are_reflected_in_snapshot() {
        VentasConsumerMonitorService mon = new VentasConsumerMonitorService();
        mon.recordProcessed("A", "id1");
        mon.recordProcessed("A", "id2");
        mon.recordError("B", "id3");
        mon.recordDuplicate("id1");

        VentasConsumerMonitorService.Snapshot s = mon.snapshot();
        assertEquals(2L, s.handledByType.get("A"));
        assertEquals(1L, s.errorsByType.get("B"));
        assertEquals(1L, s.duplicates);
        assertTrue(s.lastEventIds.contains("id1"));
        assertTrue(s.uptimeMs >= 0);
    }
}

