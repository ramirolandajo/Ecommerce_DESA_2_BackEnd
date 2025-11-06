package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {
    @Test
    void testDefaultConstructor() {
        Event e = new Event();
        assertNull(e.getId());
        assertNull(e.getType());
        assertNull(e.getPayload());
        assertNull(e.getTimestamp());
    }

    @Test
    void testParameterizedConstructor() {
        Event e = new Event("TEST_TYPE", "test payload");
        assertEquals("TEST_TYPE", e.getType());
        assertEquals("test payload", e.getPayload());
        assertNotNull(e.getTimestamp());
    }

    @Test
    void testSettersAndGetters() {
        Event e = new Event();
        e.setId(1);
        e.setType("EVENT_TYPE");
        e.setPayload("payload data");
        LocalDateTime now = LocalDateTime.now();
        e.setTimestamp(now);

        assertEquals(1, e.getId());
        assertEquals("EVENT_TYPE", e.getType());
        assertEquals("payload data", e.getPayload());
        assertEquals(now, e.getTimestamp());
    }

    @Test
    void testToString() {
        Event e = new Event("TYPE", "PAYLOAD");
        String s = e.toString();
        assertTrue(s.contains("Event"));
        assertTrue(s.contains("TYPE"));
        assertTrue(s.contains("PAYLOAD"));
        assertTrue(s.contains("timestamp"));
    }

    @Test
    void testEqualsAndHashCode() {
        Event e1 = new Event();
        e1.setId(1);
        e1.setType("T");
        Event e2 = new Event();
        e2.setId(1);
        e2.setType("T");
        Event e3 = new Event();
        e3.setId(2);

        assertTrue(e1.equals(e2));
        assertEquals(e1.hashCode(), e2.hashCode());
        assertFalse(e1.equals(e3));
    }
}
