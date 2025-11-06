package ar.edu.uade.ecommerce.ventas;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventMessageTests {

    @Test
    void normalizedEventType_lowers_and_removes_accents() {
        EventMessage m = new EventMessage();
        m.setEventType("PÁTCH: Categoría ACTIVADA");
        assertEquals("patch: categoria activada", m.getNormalizedEventType());
    }

    @Test
    void timestamp_parses_iso_and_epoch() {
        EventMessage m1 = new EventMessage();
        m1.setTimestamp(OffsetDateTime.now().toString());
        assertNotNull(m1.getTimestampAsOffsetDateTime());

        EventMessage m2 = new EventMessage();
        m2.setTimestamp(1700000000L);
        assertNotNull(m2.getTimestampAsOffsetDateTime());

        EventMessage m3 = new EventMessage();
        m3.setTimestamp("1700000000.5");
        assertNotNull(m3.getTimestampAsOffsetDateTime());
    }
}

