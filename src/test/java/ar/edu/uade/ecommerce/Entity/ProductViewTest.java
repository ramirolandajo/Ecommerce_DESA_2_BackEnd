package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ProductViewTest {
    @Test
    void constructorAndGettersWork() {
        User u = new User();
        u.setId(2);
        Product p = new Product();
        p.setId(3);
        LocalDateTime now = LocalDateTime.now();
        ProductView pv = new ProductView(u, p, now);
        assertEquals(u, pv.getUser());
        assertEquals(p, pv.getProduct());
        assertEquals(now, pv.getViewedAt());
    }

    @Test
    void settersWork() {
        ProductView pv = new ProductView();
        User u = new User(); u.setId(9);
        Product p = new Product(); p.setId(11);
        LocalDateTime t = LocalDateTime.of(2020,1,1,0,0);
        pv.setUser(u);
        pv.setProduct(p);
        pv.setViewedAt(t);
        assertEquals(u, pv.getUser());
        assertEquals(p, pv.getProduct());
        assertEquals(t, pv.getViewedAt());
    }
}

