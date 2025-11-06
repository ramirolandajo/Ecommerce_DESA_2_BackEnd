package ar.edu.uade.ecommerce.ventas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VentasEventsListenerMoreTests {

    @Test
    void onMessage_null_message_is_ignored() {
        EventIdempotencyService idem = mock(EventIdempotencyService.class);
        VentasConsumerMonitorService monitor = mock(VentasConsumerMonitorService.class);
        VentasEventsListener l = new VentasEventsListener(idem, monitor);

        l.onMessage(null);

        verifyNoInteractions(idem);
        verifyNoInteractions(monitor);
    }

    @Test
    void onMessage_when_markProcessed_throws_records_error_and_rethrows() {
        EventIdempotencyService idem = mock(EventIdempotencyService.class);
        VentasConsumerMonitorService monitor = mock(VentasConsumerMonitorService.class);
        VentasEventsListener l = new VentasEventsListener(idem, monitor);

        EventMessage m = new EventMessage();
        m.setEventId("vx");
        m.setEventType("POST: Compra confirmada");
        when(idem.alreadyProcessed("vx")).thenReturn(false);
        doThrow(new RuntimeException("fail")).when(idem).markProcessed("vx");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> l.onMessage(m));
        assertEquals("fail", ex.getMessage());
        verify(monitor).recordError("POST: Compra confirmada", "vx");
        verify(monitor, never()).recordProcessed(any(), any());
    }

    @Test
    void onMessage_with_null_eventId_flows_and_uses_null_in_services() {
        EventIdempotencyService idem = mock(EventIdempotencyService.class);
        VentasConsumerMonitorService monitor = mock(VentasConsumerMonitorService.class);
        VentasEventsListener l = new VentasEventsListener(idem, monitor);

        EventMessage m = new EventMessage();
        m.setEventId(null);
        m.setEventType("DELETE: Compra cancelada");
        when(idem.alreadyProcessed(null)).thenReturn(false);

        l.onMessage(m);

        verify(idem).alreadyProcessed(null);
        verify(idem).markProcessed(null);
        verify(monitor).recordProcessed("DELETE: Compra cancelada", null);
    }
}

