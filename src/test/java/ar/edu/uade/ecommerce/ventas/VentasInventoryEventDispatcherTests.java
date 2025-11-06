package ar.edu.uade.ecommerce.ventas;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class VentasInventoryEventDispatcherTests {

    @Test
    void process_routes_known_events_to_service_methods() {
        VentasInventorySyncService sync = mock(VentasInventorySyncService.class);
        VentasInventoryEventDispatcher dispatcher = new VentasInventoryEventDispatcher(sync);

        EventMessage m1 = new EventMessage();
        m1.setEventType("PUT: Actualizar stock");
        m1.setPayload(new Object());
        dispatcher.process(m1);
        verify(sync).actualizarStock(any());

        EventMessage m2 = new EventMessage();
        m2.setEventType("POST: Agregar un producto");
        dispatcher.process(m2);
        verify(sync).crearProducto(any());

        EventMessage m2b = new EventMessage();
        m2b.setEventType("POST: Producto creado");
        dispatcher.process(m2b);
        verify(sync, times(2)).crearProducto(any());

        EventMessage m3 = new EventMessage();
        m3.setEventType("PATCH: Modificar un producto");
        dispatcher.process(m3);
        verify(sync).modificarProducto(any());

        EventMessage m4 = new EventMessage();
        m4.setEventType("PATCH: Producto desactivado");
        dispatcher.process(m4);
        verify(sync).desactivarProducto(any());

        EventMessage m5 = new EventMessage();
        m5.setEventType("PATCH: Producto activado");
        dispatcher.process(m5);
        verify(sync, times(1)).activarProducto(any());

        EventMessage m5alias = new EventMessage();
        m5alias.setEventType("PATCH: Activar producto");
        dispatcher.process(m5alias);
        verify(sync, times(2)).activarProducto(any());

        EventMessage m6 = new EventMessage();
        m6.setEventType("PUT: Producto actualizado");
        dispatcher.process(m6);
        verify(sync).upsertProducto(any());

        EventMessage m7 = new EventMessage();
        m7.setEventType("POST: Marca creada");
        dispatcher.process(m7);
        verify(sync).crearMarca(any());

        EventMessage m8 = new EventMessage();
        m8.setEventType("PATCH: Marca activada");
        dispatcher.process(m8);
        verify(sync).activarMarca(any());

        EventMessage m9 = new EventMessage();
        m9.setEventType("PATCH: Marca desactivada");
        dispatcher.process(m9);
        verify(sync).desactivarMarca(any());

        EventMessage m10 = new EventMessage();
        m10.setEventType("POST: Categoria creada");
        dispatcher.process(m10);
        verify(sync).crearCategoria(any());

        EventMessage m11 = new EventMessage();
        m11.setEventType("PATCH: Categoría activada"); // con acento
        dispatcher.process(m11);
        verify(sync).activarCategoria(any());

        EventMessage m12 = new EventMessage();
        m12.setEventType("PATCH: categoria desactivada");
        dispatcher.process(m12);
        verify(sync).desactivarCategoria(any());

        EventMessage m13 = new EventMessage();
        m13.setEventType("POST: Agregar productos (batch)");
        dispatcher.process(m13);
        verify(sync).crearProductosBatch(any());

        EventMessage m13alias = new EventMessage();
        m13alias.setEventType("POST: Agregar productos batch");
        dispatcher.process(m13alias);
        verify(sync, times(2)).crearProductosBatch(any());
    }

    @Test
    void process_unknown_event_does_not_throw() {
        VentasInventorySyncService sync = mock(VentasInventorySyncService.class);
        VentasInventoryEventDispatcher dispatcher = new VentasInventoryEventDispatcher(sync);
        EventMessage m = new EventMessage();
        m.setEventType("PATCH: inexistente");
        dispatcher.process(m); // no debe lanzar
        verifyNoInteractions(sync);
    }
}

