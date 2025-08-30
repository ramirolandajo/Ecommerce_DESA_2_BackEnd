package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.RegisterUserDTO;
import ar.edu.uade.ecommerce.Entity.DTO.UserLoginResponseDTO;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Mock
    private AuthService authService;
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@email.com");
        request.put("password", "1234");
        User user = new User();
        user.setId(1);
        user.setName("Test");
        user.setLastname("User");
        user.setEmail("test@email.com");
        user.setSessionActive(true);
        when(authService.login("test@email.com", "1234")).thenReturn("token123");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        doNothing().when(authService).saveUser(any(User.class));
        ResponseEntity<UserLoginResponseDTO> response = authController.login(request);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("token123", response.getBody().getBearer_token());
        assertEquals("Test", response.getBody().getUser().getName());
    }

    @Test
    void testRegister_NewUser() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setEmail("new@email.com");
        User newUser = new User();
        newUser.setId(2);
        newUser.setName("Nuevo");
        newUser.setLastname("Usuario");
        newUser.setEmail("new@email.com");
        newUser.setRole("USER");
        when(authService.getUserByEmail("new@email.com")).thenReturn(null);
        when(authService.registerDTO(dto)).thenReturn(newUser);
        ResponseEntity<String> response = authController.register(dto);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Fuiste registrado satisfactoriamente"));
    }

    @Test
    void testRegister_ExistingActiveUser() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setEmail("exist@email.com");
        User existingUser = new User();
        existingUser.setEmail("exist@email.com");
        existingUser.setAccountActive(true);
        when(authService.getUserByEmail("exist@email.com")).thenReturn(existingUser);
        ResponseEntity<String> response = authController.register(dto);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("La cuenta ya está activa"));
    }

    @Test
    void testRegister_ExistingInactiveUser() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setEmail("inactive@email.com");
        User existingUser = new User();
        existingUser.setEmail("inactive@email.com");
        existingUser.setAccountActive(false);
        when(authService.getUserByEmail("inactive@email.com")).thenReturn(existingUser);
        doNothing().when(authService).resendVerificationToken(existingUser);
        ResponseEntity<String> response = authController.register(dto);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("La cuenta ya existe pero no está activa"));
    }

    @Test
    void testVerifyEmail_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@email.com");
        request.put("token", "token123");
        when(authService.verifyEmailToken("test@email.com", "token123")).thenReturn(true);
        ResponseEntity<String> response = authController.verifyEmail(request);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Cuenta verificada exitosamente"));
    }

    @Test
    void testVerifyEmail_Failure() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@email.com");
        request.put("token", "token123");
        when(authService.verifyEmailToken("test@email.com", "token123")).thenReturn(false);
        ResponseEntity<String> response = authController.verifyEmail(request);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Token inválido o expirado"));
    }

    @Test
    void testLogout_Success() {
        String token = "Bearer token123";
        User user = new User();
        user.setSessionActive(true);
        when(authService.getEmailFromToken("token123")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        doNothing().when(authService).saveUser(any(User.class));
        ResponseEntity<String> response = authController.logout(token);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Sesión cerrada correctamente"));
    }
}
