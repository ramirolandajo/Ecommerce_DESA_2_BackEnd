package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    @Test
    void testSettersAndGetters() {
        User user = new User();
        user.setId(10);
        user.setName("Juan");
        user.setLastname("Pérez");
        user.setEmail("juan@example.com");
        user.setPassword("1234");
        user.setRole("USER");
        user.setAccountActive(true);
        user.setAddresses(List.of());
        assertEquals(10, user.getId());
        assertEquals("Juan", user.getName());
        assertEquals("Pérez", user.getLastname());
        assertEquals("juan@example.com", user.getEmail());
        assertEquals("1234", user.getPassword());
        assertEquals("USER", user.getRole());
        assertTrue(user.isAccountActive());
        assertNotNull(user.getAddresses());
    }

    @Test
    void testIsEmpty() {
        User user = new User();
        assertTrue(user.isEmpty());
        user.setId(1);
        user.setName("A");
        user.setLastname("B");
        user.setEmail("C");
        user.setPassword("D");
        user.setRole("E");
        assertFalse(user.isEmpty());
    }

    @Test
    void testIsEmptyBranches() {
        User user = new User();
        // Branch: todos null
        assertTrue(user.isEmpty());
        // Branch: id no null
        user.setId(1);
        assertFalse(user.isEmpty());
        user.setId(null);
        // Branch: name no null
        user.setName("A");
        assertFalse(user.isEmpty());
        user.setName(null);
        // Branch: lastname no null
        user.setLastname("B");
        assertFalse(user.isEmpty());
        user.setLastname(null);
        // Branch: email no null
        user.setEmail("C");
        assertFalse(user.isEmpty());
        user.setEmail(null);
        // Branch: password no null
        user.setPassword("D");
        assertFalse(user.isEmpty());
        user.setPassword(null);
        // Branch: role no null
        user.setRole("E");
        assertFalse(user.isEmpty());
        user.setRole(null);
    }
}
