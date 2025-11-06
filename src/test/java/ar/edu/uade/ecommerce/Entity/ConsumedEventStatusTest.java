package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumedEventStatusTest {
    @Test
    void testEnumValues() {
        assertEquals(3, ConsumedEventStatus.values().length);
        assertEquals(ConsumedEventStatus.PENDING, ConsumedEventStatus.valueOf("PENDING"));
        assertEquals(ConsumedEventStatus.PROCESSED, ConsumedEventStatus.valueOf("PROCESSED"));
        assertEquals(ConsumedEventStatus.ERROR, ConsumedEventStatus.valueOf("ERROR"));
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, ConsumedEventStatus.PENDING.ordinal());
        assertEquals(1, ConsumedEventStatus.PROCESSED.ordinal());
        assertEquals(2, ConsumedEventStatus.ERROR.ordinal());
    }
}
