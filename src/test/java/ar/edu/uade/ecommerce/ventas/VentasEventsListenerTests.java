package ar.edu.uade.ecommerce.ventas;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class VentasEventsListenerTests {

    @Test
    void onMessage_processes_and_marks_idempotent() {
        EventIdempotencyService idem = mock(EventIdempotencyService.class);
        VentasConsumerMonitorService monitor = mock(VentasConsumerMonitorService.class);
        VentasEventsListener l = new VentasEventsListener(idem, monitor);

        EventMessage m = new EventMessage();
        m.setEventId("v1");
        m.setEventType("POST: Compra confirmada");
        when(idem.alreadyProcessed("v1")).thenReturn(false);

        l.onMessage(m);

        verify(idem).markProcessed("v1");
        verify(monitor).recordProcessed("POST: Compra confirmada", "v1");
    }

    @Test
    void onMessage_duplicate_is_counted_and_skipped() {
        EventIdempotencyService idem = mock(EventIdempotencyService.class);
        VentasConsumerMonitorService monitor = mock(VentasConsumerMonitorService.class);
        VentasEventsListener l = new VentasEventsListener(idem, monitor);

        EventMessage m = new EventMessage();
        m.setEventId("v2");
        m.setEventType("POST: Compra confirmada");
        when(idem.alreadyProcessed("v2")).thenReturn(true);

        l.onMessage(m);

        verify(monitor).recordDuplicate("v2");
        verify(idem, never()).markProcessed(any());
    }

    @Test
    void onMessage_unknown_type_is_ignored_without_error() {
        EventIdempotencyService idem = mock(EventIdempotencyService.class);
        VentasConsumerMonitorService monitor = mock(VentasConsumerMonitorService.class);
        VentasEventsListener l = new VentasEventsListener(idem, monitor);

        EventMessage m = new EventMessage();
        m.setEventId("v4");
        m.setEventType("PATCH: Tipo desconocido");
        when(idem.alreadyProcessed("v4")).thenReturn(false);

        // validar normalización y usar la variable
        assertEquals("patch: tipo desconocido", m.getNormalizedEventType());

        l.onMessage(m); // no debería lanzar ni marcar procesado

        verify(idem).markProcessed("v4");
        verify(monitor).recordProcessed("PATCH: Tipo desconocido", "v4");
    }
}
