package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {
    @Test
    void testSetIsNewWithNull() {
        Product product = new Product();
        product.setIsNew(null);
        assertFalse(product.getIsNew());
    }

    @Test
    void testSetIsNewWithTrueString() {
        Product product = new Product();
        product.setIsNew("true");
        assertTrue(product.getIsNew());
    }

    @Test
    void testSetIsNewWithFalseString() {
        Product product = new Product();
        product.setIsNew("false");
        assertFalse(product.getIsNew());
    }

    @Test
    void testSetIsNewWithBooleanTrue() {
        Product product = new Product();
        product.setIsNew(Boolean.TRUE);
        assertTrue(product.getIsNew());
    }

    @Test
    void testSetIsNewWithBooleanFalse() {
        Product product = new Product();
        product.setIsNew(Boolean.FALSE);
        assertFalse(product.getIsNew());
    }

    @Test
    void testSetIsNewWithNumber() {
        Product product = new Product();
        product.setIsNew(1);
        // Boolean.parseBoolean("1") es false
        assertFalse(product.getIsNew());
    }
}
