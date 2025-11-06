package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    @Test
    void isEmpty_whenNewAndNoFieldsSet() {
        User u = new User();
        assertTrue(u.isEmpty());
    }

    @Test
    void isEmpty_whenSomeFieldsSet() {
        User u = new User();
        u.setEmail("a@b.com");
        assertFalse(u.isEmpty());
    }

    @Test
    void gettersAndSettersWork() {
        User u = new User();
        u.setId(10);
        u.setName("John");
        u.setLastname("Doe");
        u.setEmail("john@doe.com");
        u.setPassword("pass");
        u.setRole("USER");
        u.setAccountActive(true);

        assertEquals(10, u.getId());
        assertEquals("John", u.getName());
        assertEquals("Doe", u.getLastname());
        assertEquals("john@doe.com", u.getEmail());
        assertEquals("pass", u.getPassword());
        assertEquals("USER", u.getRole());
        assertTrue(u.isAccountActive());
    }
}

