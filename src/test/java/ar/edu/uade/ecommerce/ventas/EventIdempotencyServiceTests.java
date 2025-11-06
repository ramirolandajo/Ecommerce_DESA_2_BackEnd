package ar.edu.uade.ecommerce.ventas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventIdempotencyServiceTests {

    @Test
    void alreadyProcessed_false_then_true_after_mark() {
        EventIdempotencyService svc = new EventIdempotencyService();
        String id = "evt-1";
        assertFalse(svc.alreadyProcessed(id));
        svc.markProcessed(id);
        assertTrue(svc.alreadyProcessed(id));
    }

    @Test
    void null_or_blank_ids_are_ignored() {
        EventIdempotencyService svc = new EventIdempotencyService();
        assertFalse(svc.alreadyProcessed(null));
        assertFalse(svc.alreadyProcessed(""));
        svc.markProcessed(null);
        svc.markProcessed(" ");
        assertFalse(svc.alreadyProcessed(null));
        assertFalse(svc.alreadyProcessed(" "));
    }
}

