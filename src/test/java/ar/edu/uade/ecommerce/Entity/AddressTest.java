package ar.edu.uade.ecommerce.Entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AddressTest {
    @Test
    void testEqualsAndHashCodeBranches() {
        Address a1 = new Address();
        Address a2 = new Address();
        // Ambos description y user null
        assertTrue(a1.equals(a2));
        assertEquals(a1.hashCode(), a2.hashCode());

        // Solo description null, user null
        a1.setDescription(null);
        a2.setDescription(null);
        assertTrue(a1.equals(a2));

        // Description igual, user null
        a1.setDescription("desc");
        a2.setDescription("desc");
        assertTrue(a1.equals(a2));

        // Description diferente
        a2.setDescription("otra");
        assertFalse(a1.equals(a2));

        // Description igual, user null, user con id null
        a1.setDescription("desc");
        a2.setDescription("desc");
        User u1 = new User();
        User u2 = new User();
        a1.setUser(u1);
        a2.setUser(u2);
        assertTrue(a1.equals(a2));

        // User con id igual
        u1.setId(1);
        u2.setId(1);
        assertTrue(a1.equals(a2));
        assertEquals(a1.hashCode(), a2.hashCode());

        // User con id diferente
        u2.setId(2);
        assertFalse(a1.equals(a2));

        // Uno de los users null
        a1.setUser(null);
        assertFalse(a1.equals(a2));
    }

    @Test
    void testEqualsWithOtherClass() {
        Address address = new Address();
        assertFalse(address.equals("string"));
    }

    @Test
    void testEqualsWithNull() {
        Address address = new Address();
        assertFalse(address.equals(null));
    }

    @Test
    void testEqualsWithSelf() {
        Address address = new Address();
        assertTrue(address.equals(address));
    }

    @Test
    void testHashCodeBranches() {
        Address a1 = new Address();
        // Ambos nulos
        assertEquals(0, a1.hashCode());
        // Solo description
        a1.setDescription("desc");
        Address a2 = new Address();
        a2.setDescription("desc");
        assertEquals(a1.hashCode(), a2.hashCode());
        // Description y user con id
        User u = new User();
        u.setId(5);
        a1.setUser(u);
        User u2 = new User();
        u2.setId(5);
        a2.setUser(u2);
        assertEquals(a1.hashCode(), a2.hashCode());
        // Description y user con id null
        User u3 = new User();
        a1.setUser(u3);
        a2.setUser(u3);
        assertEquals(a1.hashCode(), a2.hashCode());
        // Description y user null
        a1.setUser(null);
        a2.setUser(null);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void testEqualsWithUserIdNullVsUserIdNotNull() {
        Address a1 = new Address();
        Address a2 = new Address();
        a1.setDescription("desc");
        a2.setDescription("desc");
        User u1 = new User(); // id null
        User u2 = new User();
        u2.setId(5); // id no null
        a1.setUser(u1);
        a2.setUser(u2);
        // Deben ser distintos porque uno tiene id y el otro no
        assertFalse(a1.equals(a2));
        assertFalse(a2.equals(a1));
    }

    @Test
    void testEqualsWithDescriptionNullVsNotNull() {
        Address a1 = new Address();
        Address a2 = new Address();
        a1.setDescription(null);
        a2.setDescription("desc");
        // Deben ser distintos porque uno tiene description null y el otro no
        assertFalse(a1.equals(a2));
        assertFalse(a2.equals(a1));
    }
}
