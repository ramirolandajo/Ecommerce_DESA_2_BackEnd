package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {
    @Test
    void booleanSettersAcceptDifferentTypes() {
        Product p = new Product();
        p.setIsNew("true");
        assertTrue(p.getIsNew());
        p.setIsNew(null);
        assertFalse(p.getIsNew());

        p.setIsBestseller(true);
        assertTrue(p.isIsBestseller());
        p.setIsFeatured(true);
        assertTrue(p.isIsFeatured());

        p.setActive(true);
        assertTrue(p.getActive());
    }

    @Test
    void basicFieldsWork() {
        Product p = new Product();
        p.setTitle("Prod");
        p.setPrice(12.5f);
        p.setStock(5);
        p.setProductCode(999);

        assertEquals("Prod", p.getTitle());
        assertEquals(12.5f, p.getPrice());
        assertEquals(5, p.getStock());
        assertEquals(999, p.getProductCode());
    }
}

