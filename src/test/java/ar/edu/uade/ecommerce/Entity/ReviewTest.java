package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewTest {
    @Test
    void testSettersAndGetters() {
        Review r = new Review();
        r.setId(1L);
        r.setCalification(4.5f);
        r.setDescription("Great product!");
        Product p = new Product();
        p.setId(2);
        r.setProduct(p);
        User u = new User();
        u.setId(3);
        r.setUser(u);

        assertEquals(1L, r.getId());
        assertEquals(4.5f, r.getCalification());
        assertEquals("Great product!", r.getDescription());
        assertEquals(p, r.getProduct());
        assertEquals(u, r.getUser());
    }

    @Test
    void testToString() {
        Review r = new Review();
        r.setId(1L);
        r.setCalification(5.0f);
        String s = r.toString();
        assertTrue(s.contains("Review"));
        assertTrue(s.contains("5.0"));
    }

    @Test
    void testEqualsAndHashCode() {
        Review r1 = new Review();
        r1.setId(1L);
        r1.setCalification(4.0f);
        Review r2 = new Review();
        r2.setId(1L);
        r2.setCalification(4.0f);
        Review r3 = new Review();
        r3.setId(2L);

        assertTrue(r1.equals(r2));
        assertEquals(r1.hashCode(), r2.hashCode());
        assertFalse(r1.equals(r3));
    }
}
