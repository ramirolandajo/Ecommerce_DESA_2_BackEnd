package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FavouriteProductsExtraTest {

    @Test
    void equalsAndHash_withSameId() {
        FavouriteProducts a = new FavouriteProducts();
        a.setId(1L);
        FavouriteProducts b = new FavouriteProducts();
        b.setId(1L);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsWithNullId_notEqual() {
        FavouriteProducts a = new FavouriteProducts();
        a.setId(null);
        FavouriteProducts b = new FavouriteProducts();
        b.setId(2L);
        assertNotEquals(a, b);
    }

    @Test
    void productAndUserProperties() {
        FavouriteProducts f = new FavouriteProducts();
        Product p = new Product(); p.setId(5); p.setTitle("X");
        User u = new User(); u.setId(7);
        f.setProduct(p);
        f.setUser(u);
        f.setProductCode(123);
        assertEquals(123, f.getProductCode());
        assertEquals(5, f.getProduct().getId());
        assertEquals(7, f.getUser().getId());
    }
}

