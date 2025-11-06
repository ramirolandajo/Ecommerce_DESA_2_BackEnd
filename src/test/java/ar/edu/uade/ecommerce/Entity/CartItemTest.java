package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CartItemTest {
    @Test
    void testSettersAndGetters() {
        CartItem ci = new CartItem();
        ci.setId(1);
        ci.setQuantity(5);
        Cart c = new Cart();
        c.setId(2);
        ci.setCart(c);
        Product p = new Product();
        p.setId(3);
        ci.setProduct(p);

        assertEquals(1, ci.getId());
        assertEquals(5, ci.getQuantity());
        assertEquals(c, ci.getCart());
        assertEquals(p, ci.getProduct());
    }

    @Test
    void testToString() {
        CartItem ci = new CartItem();
        ci.setId(1);
        ci.setQuantity(2);
        String s = ci.toString();
        assertTrue(s.contains("CartItem"));
        assertTrue(s.contains("2"));
    }

    @Test
    void testEqualsAndHashCode() {
        CartItem ci1 = new CartItem();
        ci1.setId(1);
        ci1.setQuantity(3);
        CartItem ci2 = new CartItem();
        ci2.setId(1);
        ci2.setQuantity(3);
        CartItem ci3 = new CartItem();
        ci3.setId(2);

        assertTrue(ci1.equals(ci2));
        assertEquals(ci1.hashCode(), ci2.hashCode());
        assertFalse(ci1.equals(ci3));
    }
}
