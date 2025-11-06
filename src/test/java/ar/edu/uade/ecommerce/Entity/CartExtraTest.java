package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartExtraTest {

    @Test
    void equalsAndHashWithSameId() {
        Cart c1 = new Cart(); c1.setId(10);
        Cart c2 = new Cart(); c2.setId(10);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void itemsList_modificationReflected() {
        Cart c = new Cart();
        CartItem it = new CartItem(); it.setId(2);
        c.getItems().add(it);
        assertEquals(1, c.getItems().size());
        assertEquals(2, c.getItems().get(0).getId());
    }
}

