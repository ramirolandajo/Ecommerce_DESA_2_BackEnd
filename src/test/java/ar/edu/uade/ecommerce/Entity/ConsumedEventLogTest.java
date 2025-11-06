package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumedEventLogTest {
    @Test
    void testSettersAndGetters() {
        ConsumedEventLog cel = new ConsumedEventLog();
        cel.setId(1L);
        cel.setEventId("event-123");
        cel.setEventType("TEST");
        cel.setOriginModule("ModuleA");
        cel.setTimestampRaw("2023-10-01T10:00:00Z");
        cel.setTopic("topic1");
        cel.setPartitionId(0);
        cel.setOffsetValue(100L);
        cel.setPayloadJson("{\"key\":\"value\"}");
        cel.setStatus(ConsumedEventStatus.PROCESSED);
        cel.setAttempts(2);
        cel.setLastError("Some error");
        OffsetDateTime now = OffsetDateTime.now();
        cel.setCreatedAt(now);
        cel.setUpdatedAt(now);
        cel.setAckSent(true);
        cel.setAckAttempts(1);
        cel.setAckLastError("Ack error");
        cel.setAckLastAt(now);

        assertEquals(1L, cel.getId());
        assertEquals("event-123", cel.getEventId());
        assertEquals("TEST", cel.getEventType());
        assertEquals("ModuleA", cel.getOriginModule());
        assertEquals("2023-10-01T10:00:00Z", cel.getTimestampRaw());
        assertEquals("topic1", cel.getTopic());
        assertEquals(0, cel.getPartitionId());
        assertEquals(100L, cel.getOffsetValue());
        assertEquals("{\"key\":\"value\"}", cel.getPayloadJson());
        assertEquals(ConsumedEventStatus.PROCESSED, cel.getStatus());
        assertEquals(2, cel.getAttempts());
        assertEquals("Some error", cel.getLastError());
        assertEquals(now, cel.getCreatedAt());
        assertEquals(now, cel.getUpdatedAt());
        assertTrue(cel.getAckSent());
        assertEquals(1, cel.getAckAttempts());
        assertEquals("Ack error", cel.getAckLastError());
        assertEquals(now, cel.getAckLastAt());
    }

    @Test
    void testDefaultValues() {
        ConsumedEventLog cel = new ConsumedEventLog();
        assertEquals(ConsumedEventStatus.PENDING, cel.getStatus());
        assertEquals(0, cel.getAttempts());
        assertFalse(cel.getAckSent());
        assertEquals(0, cel.getAckAttempts());
        assertNotNull(cel.getCreatedAt());
        assertNotNull(cel.getUpdatedAt());
    }

    @Test
    void testToString() {
        ConsumedEventLog cel = new ConsumedEventLog();
        cel.setEventId("test-event");
        cel.setStatus(ConsumedEventStatus.ERROR);
        String s = cel.toString();
        assertTrue(s.contains("ConsumedEventLog"));
        assertTrue(s.contains("test-event"));
        assertTrue(s.contains("ERROR"));
    }

    @Test
    void testEqualsAndHashCode() {
        ConsumedEventLog cel1 = new ConsumedEventLog();
        cel1.setId(1L);
        cel1.setEventId("event1");
        ConsumedEventLog cel2 = new ConsumedEventLog();
        cel2.setId(1L);
        cel2.setEventId("event1");
        ConsumedEventLog cel3 = new ConsumedEventLog();
        cel3.setId(2L);

        assertTrue(cel1.equals(cel2));
        assertEquals(cel1.hashCode(), cel2.hashCode());
        assertFalse(cel1.equals(cel3));
    }
}
