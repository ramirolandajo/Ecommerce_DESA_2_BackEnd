package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AddressService;
import ar.edu.uade.ecommerce.Service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressControllerTest {
    @Mock
    private AddressService addressService;
    @Mock
    private AuthService authService;
    @InjectMocks
    private AddressController addressController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAddressesByUser_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        List<Address> addresses = List.of(new Address(), new Address());
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(addressService.getAddressesByUser(user)).thenReturn(addresses);
        ResponseEntity<List<Address>> response = addressController.getAddressesByUser(token);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(addresses, response.getBody());
    }

    @Test
    void testGetAddressesByUser_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<List<Address>> response = addressController.getAddressesByUser(token);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testGetAddressesByUser_TokenInvalido() {
        String token = "Bearer testtoken";
        when(authService.getEmailFromToken("testtoken")).thenReturn(null);
        ResponseEntity<List<Address>> response = addressController.getAddressesByUser(token);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testGetAddressesByUser_EmailNull() {
        String token = "Bearer testtoken";
        when(authService.getEmailFromToken("testtoken")).thenReturn(null);
        when(authService.getUserByEmail(null)).thenReturn(null);
        ResponseEntity<List<Address>> response = addressController.getAddressesByUser(token);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testGetAddressesByUser_UserNull() {
        String token = "Bearer testtoken";
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<List<Address>> response = addressController.getAddressesByUser(token);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testAddAddress_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(addressService.addAddress(any(Address.class))).thenReturn(address);
        ResponseEntity<Address> response = addressController.addAddress(token, address);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(address, response.getBody());
    }

    @Test
    void testAddAddress_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<Address> response = addressController.addAddress(token, address);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testAddAddress_TokenInvalido() {
        String token = "Bearer testtoken";
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn(null);
        ResponseEntity<Address> response = addressController.addAddress(token, address);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testAddAddress_EmailNull() {
        String token = "Bearer testtoken";
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn(null);
        when(authService.getUserByEmail(null)).thenReturn(null);
        ResponseEntity<Address> response = addressController.addAddress(token, address);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testAddAddress_UserNull() {
        String token = "Bearer testtoken";
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<Address> response = addressController.addAddress(token, address);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteAddress_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        doNothing().when(addressService).deleteAddress(eq(1), eq(user));
        ResponseEntity<String> response = addressController.deleteAddress(token, 1);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Dirección eliminada con éxito", response.getBody());
    }

    @Test
    void testDeleteAddress_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<String> response = addressController.deleteAddress(token, 1);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
    }

    @Test
    void testDeleteAddress_TokenInvalido() {
        String token = "Bearer testtoken";
        when(authService.getEmailFromToken("testtoken")).thenReturn(null);
        ResponseEntity<String> response = addressController.deleteAddress(token, 1);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
    }

    @Test
    void testDeleteAddress_EmailNull() {
        String token = "Bearer testtoken";
        when(authService.getEmailFromToken("testtoken")).thenReturn(null);
        when(authService.getUserByEmail(null)).thenReturn(null);
        ResponseEntity<String> response = addressController.deleteAddress(token, 1);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
    }

    @Test
    void testDeleteAddress_UserNull() {
        String token = "Bearer testtoken";
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<String> response = addressController.deleteAddress(token, 1);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
    }
}
