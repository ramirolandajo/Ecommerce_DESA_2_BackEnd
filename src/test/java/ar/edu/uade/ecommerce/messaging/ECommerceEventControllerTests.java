package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ECommerceEventControllerTests {

    @Test
    void receiveEvent_handlesKnownTypes_withoutExceptions() {
        ECommerceEventController ctrl = new ECommerceEventController();
        CoreEvent ev1 = new CoreEvent("UpdateStock", "{}", "mod");
        assertDoesNotThrow(() -> ctrl.receiveEvent(ev1, "Bearer X"));
        CoreEvent ev2 = new CoreEvent("POST: Agregar producto", "{}", "mod");
        assertDoesNotThrow(() -> ctrl.receiveEvent(ev2, null));
        CoreEvent ev3 = new CoreEvent("CategorySync", "{}", "mod");
        assertDoesNotThrow(() -> ctrl.receiveEvent(ev3, null));
        CoreEvent ev4 = new CoreEvent("UnknownType", "{}", "mod");
        assertDoesNotThrow(() -> ctrl.receiveEvent(ev4, null));
        // adicionales
        CoreEvent ev5 = new CoreEvent("PATCH: Actualizar stock", "{}", "mod");
        assertDoesNotThrow(() -> ctrl.receiveEvent(ev5, null));
        CoreEvent ev6 = new CoreEvent("DELETE: Categoria eliminada", "{}", "mod");
        assertDoesNotThrow(() -> ctrl.receiveEvent(ev6, null));
    }

    @Test
    void receiveEvent_nullOrInvalidEvent_doesNotThrow() {
        ECommerceEventController ctrl = new ECommerceEventController();
        assertDoesNotThrow(() -> ctrl.receiveEvent(null, null));
        CoreEvent bad = new CoreEvent(null, "{}", "mod");
        assertDoesNotThrow(() -> ctrl.receiveEvent(bad, null));
    }
}
