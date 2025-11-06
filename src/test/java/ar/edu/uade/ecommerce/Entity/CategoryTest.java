package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryTest {

    @Test
    void defaultConstructorSetsActiveTrue() {
        Category c = new Category();
        assertTrue(c.isActive(), "La categoría debe estar activa por defecto");
    }

    @Test
    void gettersAndSettersWork() {
        Category c = new Category();
        c.setName("Electrónica");
        c.setCategoryCode(1234);
        c.setActive(false);

        assertEquals("Electrónica", c.getName());
        assertEquals(1234, c.getCategoryCode());
        assertFalse(c.isActive());
    }
}

