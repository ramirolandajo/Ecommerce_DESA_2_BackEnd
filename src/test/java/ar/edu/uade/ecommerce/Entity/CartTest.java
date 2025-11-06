package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CartTest {
    @Test
    void testEqualsAndHashCode() {
        Cart c1 = new Cart();
        c1.setId(1);
        Cart c2 = new Cart();
        c2.setId(1);
        Cart c3 = new Cart();
        c3.setId(2);

        assertTrue(c1.equals(c2));
        assertEquals(c1.hashCode(), c2.hashCode());
        assertFalse(c1.equals(c3));
        assertFalse(c1.equals(null));
        assertFalse(c1.equals(new Object()));
    }

    @Test
    void testEqualsWithNullId() {
        Cart c1 = new Cart();
        c1.setId(null);
        Cart c2 = new Cart();
        c2.setId(null);

        assertTrue(c1.equals(c2));
    }

    @Test
    void testToString() {
        Cart c = new Cart();
        c.setId(1);
        String s = c.toString();
        assertTrue(s.contains("1"));
        assertTrue(s.contains("Cart"));
    }

    @Test
    void testSettersAndGetters() {
        Cart c = new Cart();
        c.setId(10);
        c.setFinalPrice(100.5f);
        User u = new User();
        u.setId(5);
        c.setUser(u);

        assertEquals(10, c.getId());
        assertEquals(100.5f, c.getFinalPrice());
        assertEquals(u, c.getUser());
        assertNotNull(c.getItems());
        assertTrue(c.getItems().isEmpty());
    }

    @Test
    void testItemsList() {
        Cart c = new Cart();
        CartItem item = new CartItem();
        c.getItems().add(item);
        assertEquals(1, c.getItems().size());
        assertEquals(item, c.getItems().get(0));
    }
}
