package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PurchaseTest {
    @Test
    void testSettersAndGetters() {
        Purchase p = new Purchase();
        p.setId(1);
        p.setDirection("123 Main St");
        p.setStatus(Purchase.Status.CONFIRMED);
        LocalDateTime now = LocalDateTime.now();
        p.setDate(now);
        p.setReservationTime(now.minusHours(1));
        User u = new User();
        u.setId(2);
        p.setUser(u);
        Cart c = new Cart();
        c.setId(3);
        p.setCart(c);

        assertEquals(1, p.getId());
        assertEquals("123 Main St", p.getDirection());
        assertEquals(Purchase.Status.CONFIRMED, p.getStatus());
        assertEquals(now, p.getDate());
        assertEquals(now.minusHours(1), p.getReservationTime());
        assertEquals(u, p.getUser());
        assertEquals(c, p.getCart());
    }

    @Test
    void testStatusEnum() {
        assertEquals(3, Purchase.Status.values().length);
        assertEquals(Purchase.Status.CONFIRMED, Purchase.Status.valueOf("CONFIRMED"));
        assertEquals(Purchase.Status.PENDING, Purchase.Status.valueOf("PENDING"));
        assertEquals(Purchase.Status.CANCELLED, Purchase.Status.valueOf("CANCELLED"));
    }

    @Test
    void testToString() {
        Purchase p = new Purchase();
        p.setId(1);
        p.setStatus(Purchase.Status.PENDING);
        String s = p.toString();
        assertTrue(s.contains("Purchase"));
        assertTrue(s.contains("PENDING"));
    }

    @Test
    void testEqualsAndHashCode() {
        Purchase p1 = new Purchase();
        p1.setId(1);
        p1.setStatus(Purchase.Status.CONFIRMED);
        Purchase p2 = new Purchase();
        p2.setId(1);
        p2.setStatus(Purchase.Status.CONFIRMED);
        Purchase p3 = new Purchase();
        p3.setId(2);

        // Since @Data uses all fields, but id is part of it
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());
        assertFalse(p1.equals(p3));
    }
}
