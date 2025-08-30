package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Service.AuthService;
import ar.edu.uade.ecommerce.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private AuthService authService;
    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCurrentUser_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<User> response = userController.getCurrentUser(token);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetCurrentUser_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<User> response = userController.getCurrentUser(token);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testAddAddress_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.addAddress(any(Address.class))).thenReturn(address);
        ResponseEntity<?> response = userController.addAddress(token, address);
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
        ResponseEntity<?> response = userController.addAddress(token, address);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testUpdateAddress_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.updateAddress(eq(1), any(Address.class))).thenReturn(address);
        ResponseEntity<?> response = userController.updateAddress(token, 1, address);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(address, response.getBody());
    }

    @Test
    void testDeleteAddress_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        doNothing().when(userService).deleteAddress(eq(1), eq(user));
        ResponseEntity<?> response = userController.deleteAddress(token, 1);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Dirección eliminada con éxito", response.getBody());
    }

    @Test
    void testUpdateUserData_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        User userPatch = new User();
        userPatch.setName("NuevoNombre");
        userPatch.setLastname("NuevoApellido");
        userPatch.setPassword("NuevaPassword");
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.saveUser(any(User.class))).thenReturn(user);
        ResponseEntity<?> response = userController.updateUserData(token, userPatch);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetUserAddresses_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        List<Address> addresses = List.of(new Address(), new Address());
        user.setAddresses(addresses);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<List<Address>> response = userController.getUserAddresses(token);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(addresses, response.getBody());
    }
}

