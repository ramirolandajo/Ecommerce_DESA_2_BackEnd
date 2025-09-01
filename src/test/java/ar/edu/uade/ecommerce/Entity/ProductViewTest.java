package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductViewTest {
    @Test
    void testProductViewConstructorAndGetters() {
        User user = new User();
        Product product = new Product();
        LocalDateTime now = LocalDateTime.now();
        ProductView view = new ProductView(user, product, now);
        assertEquals(user, view.getUser());
        assertEquals(product, view.getProduct());
        assertEquals(now, view.getViewedAt());
    }

    @Test
    void testSetters() {
        ProductView view = new ProductView();
        User user = new User();
        Product product = new Product();
        LocalDateTime now = LocalDateTime.now();
        view.setUser(user);
        view.setProduct(product);
        view.setViewedAt(now);
        assertEquals(user, view.getUser());
        assertEquals(product, view.getProduct());
        assertEquals(now, view.getViewedAt());
    }
}
