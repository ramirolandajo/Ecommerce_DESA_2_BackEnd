package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FavouriteProductsTest {
    @Test
    void testSettersAndGetters() {
        FavouriteProducts fp = new FavouriteProducts();
        fp.setId(1L);
        fp.setProductCode(123);
        Product p = new Product();
        p.setId(2);
        fp.setProduct(p);
        User u = new User();
        u.setId(3);
        fp.setUser(u);

        assertEquals(1L, fp.getId());
        assertEquals(123, fp.getProductCode());
        assertEquals(p, fp.getProduct());
        assertEquals(u, fp.getUser());
    }

    @Test
    void testToString() {
        FavouriteProducts fp = new FavouriteProducts();
        fp.setId(1L);
        fp.setProductCode(456);
        String s = fp.toString();
        assertTrue(s.contains("FavouriteProducts"));
        assertTrue(s.contains("456"));
    }

    @Test
    void testEqualsAndHashCode() {
        FavouriteProducts fp1 = new FavouriteProducts();
        fp1.setId(1L);
        fp1.setProductCode(100);
        FavouriteProducts fp2 = new FavouriteProducts();
        fp2.setId(1L);
        fp2.setProductCode(100);
        FavouriteProducts fp3 = new FavouriteProducts();
        fp3.setId(2L);

        assertTrue(fp1.equals(fp2));
        assertEquals(fp1.hashCode(), fp2.hashCode());
        assertFalse(fp1.equals(fp3));
    }

    @Test
    void testEqualsWithNullId() {
        FavouriteProducts fp1 = new FavouriteProducts();
        fp1.setId(null);
        FavouriteProducts fp2 = new FavouriteProducts();
        fp2.setId(null);

        assertTrue(fp1.equals(fp2));
    }

    @Test
    void testEqualsWithDifferentProductCode() {
        FavouriteProducts fp1 = new FavouriteProducts();
        fp1.setId(1L);
        fp1.setProductCode(100);
        FavouriteProducts fp2 = new FavouriteProducts();
        fp2.setId(1L);
        fp2.setProductCode(200);

        assertFalse(fp1.equals(fp2));
    }

    @Test
    void testToStringWithNullValues() {
        FavouriteProducts fp = new FavouriteProducts();
        String s = fp.toString();
        assertTrue(s.contains("FavouriteProducts"));
    }
}
