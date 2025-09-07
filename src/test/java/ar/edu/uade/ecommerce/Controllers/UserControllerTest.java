package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Service.AuthService;
import ar.edu.uade.ecommerce.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private AuthService authService;
    @InjectMocks
    private UserController userController;

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
    void testGetCurrentUser_ActiveUserNoAddresses() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        user.setAddresses(List.of()); // lista vacía
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<User> response = userController.getCurrentUser(token);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
        assertTrue(response.getBody().getAddresses().isEmpty());
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
    void testAddAddress_ActiveUserNewAddress() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Address address = new Address();
        address.setUser(user);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.addAddress(any(Address.class))).thenReturn(address);
        ResponseEntity<?> response = userController.addAddress(token, address);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(address, response.getBody());
        assertEquals(user, ((Address)response.getBody()).getUser());
    }

    @Test
    void testAddAddress_AddressAlreadyExists() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Address address = new Address();
        List<Address> addresses = List.of(address);
        user.setAddresses(addresses);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = userController.addAddress(token, address);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("La dirección ya existe para el usuario", response.getBody());
    }

    @Test
    void testAddAddress_AddressAlreadyExistsAmongMany() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Address address1 = new Address();
        Address address2 = new Address();
        Address address3 = new Address();
        // address2 será igual al address que se intenta agregar
        List<Address> addresses = List.of(address1, address2, address3);
        user.setAddresses(addresses);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = userController.addAddress(token, address2);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("La dirección ya existe para el usuario", response.getBody());
    }

    @Test
    void testAddAddress_AddressNotExistsAmongMany() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setId(1); // Asegura que el usuario tenga un id
        user.setSessionActive(true);
        Address address1 = new Address();
        address1.setDescription("Calle 1");
        address1.setUser(user);
        Address address2 = new Address();
        address2.setDescription("Calle 2");
        address2.setUser(user);
        Address address3 = new Address();
        address3.setDescription("Calle 3");
        address3.setUser(user);
        Address newAddress = new Address(); // no está en la lista
        newAddress.setDescription("Calle 4");
        newAddress.setUser(user);
        List<Address> addresses = List.of(address1, address2, address3);
        user.setAddresses(addresses);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.addAddress(any(Address.class))).thenReturn(newAddress);
        ResponseEntity<?> response = userController.addAddress(token, newAddress);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(newAddress, response.getBody());
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
    void testUpdateAddress_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = userController.updateAddress(token, 1, address);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
    }

    @Test
    void testUpdateAddress_UserNull() {
        String token = "Bearer testtoken";
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<?> response = userController.updateAddress(token, 1, address);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
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
    void testDeleteAddress_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = userController.deleteAddress(token, 1);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
    }

    @Test
    void testDeleteAddress_UserNull() {
        String token = "Bearer testtoken";
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<?> response = userController.deleteAddress(token, 1);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
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
    void testUpdateUserData_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        User userPatch = new User();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = userController.updateUserData(token, userPatch);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
    }

    @Test
    void testUpdateUserData_UserNull() {
        String token = "Bearer testtoken";
        User userPatch = new User();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<?> response = userController.updateUserData(token, userPatch);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
    }

    @Test
    void testUpdateUserData_PatchNameOnly() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        user.setName("Original");
        User userPatch = new User();
        userPatch.setName("NuevoNombre");
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.saveUser(any(User.class))).thenReturn(user);
        ResponseEntity<?> response = userController.updateUserData(token, userPatch);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("NuevoNombre", ((User)response.getBody()).getName());
    }

    @Test
    void testUpdateUserData_PatchLastnameOnly() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        user.setLastname("OriginalApellido");
        User userPatch = new User();
        userPatch.setLastname("NuevoApellido");
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.saveUser(any(User.class))).thenReturn(user);
        ResponseEntity<?> response = userController.updateUserData(token, userPatch);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("NuevoApellido", ((User)response.getBody()).getLastname());
    }

    @Test
    void testUpdateUserData_PatchPasswordOnly() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        user.setPassword("OriginalPassword");
        User userPatch = new User();
        userPatch.setPassword("NuevaPassword");
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.saveUser(any(User.class))).thenReturn(user);
        ResponseEntity<?> response = userController.updateUserData(token, userPatch);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("NuevaPassword", ((User)response.getBody()).getPassword());
    }

    @Test
    void testUpdateUserData_PatchPasswordEmpty() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        user.setPassword("OriginalPassword");
        User userPatch = new User();
        userPatch.setPassword(""); // password vacío
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.saveUser(any(User.class))).thenReturn(user);
        ResponseEntity<?> response = userController.updateUserData(token, userPatch);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("OriginalPassword", ((User)response.getBody()).getPassword());
    }

    @Test
    void testUpdateUserData_PatchNone() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        user.setName("Original");
        user.setLastname("OriginalApellido");
        user.setPassword("OriginalPassword");
        User userPatch = new User(); // todos null
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.saveUser(any(User.class))).thenReturn(user);
        ResponseEntity<?> response = userController.updateUserData(token, userPatch);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Original", ((User)response.getBody()).getName());
        assertEquals("OriginalApellido", ((User)response.getBody()).getLastname());
        assertEquals("OriginalPassword", ((User)response.getBody()).getPassword());
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

    @Test
    void testGetUserAddresses_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<List<Address>> response = userController.getUserAddresses(token);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testGetUserAddresses_UserNull() {
        String token = "Bearer testtoken";
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<List<Address>> response = userController.getUserAddresses(token);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testGetCurrentUser_TokenNull() {
        String token = null;
        ResponseEntity<User> response = userController.getCurrentUser(token);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testGetCurrentUser_EmailNull() {
        String token = "Bearer testtoken";
        when(authService.getEmailFromToken("testtoken")).thenReturn(null);
        when(authService.getUserByEmail(null)).thenReturn(null);
        ResponseEntity<User> response = userController.getCurrentUser(token);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testAddAddress_AddressNull() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = userController.addAddress(token, null);
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testAddAddress_UserAddressesNull() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        user.setAddresses(null); // addresses es null
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.addAddress(any(Address.class))).thenReturn(address);
        ResponseEntity<?> response = userController.addAddress(token, address);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(address, response.getBody());
    }

    @Test
    void testAddAddress_UserAddressesEmpty() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        user.setAddresses(List.of()); // addresses vacío
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(userService.addAddress(any(Address.class))).thenReturn(address);
        ResponseEntity<?> response = userController.addAddress(token, address);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(address, response.getBody());
    }

    @Test
    void testAddAddress_TokenNull() {
        String token = null;
        Address address = new Address();
        ResponseEntity<?> response = userController.addAddress(token, address);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Usuario no logueado", response.getBody());
    }

    @Test
    void testAddAddress_UserNull() {
        String token = "Bearer testtoken";
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<?> response = userController.addAddress(token, address);
        assertEquals(401, response.getStatusCode().value());
        assertEquals("Usuario no logueado", response.getBody());
    }

    @Test
    void testAddAddress_UserSessionInactive() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        Address address = new Address();
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = userController.addAddress(token, address);
        assertEquals(401, response.getStatusCode().value());
        assertEquals("Usuario no logueado", response.getBody());
    }

    @Test
    void testAddAddress_AddressAlreadyExists_EqualsBranch() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Address address = new Address();
        List<Address> addresses = List.of(address);
        user.setAddresses(addresses);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = userController.addAddress(token, address);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("La dirección ya existe para el usuario", response.getBody());
    }
}
