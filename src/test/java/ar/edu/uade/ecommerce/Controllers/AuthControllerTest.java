package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.RegisterUserDTO;
import ar.edu.uade.ecommerce.Entity.DTO.UserLoginResponseDTO;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import ar.edu.uade.ecommerce.Exceptions.InvalidCredentialsException;
import ar.edu.uade.ecommerce.Exceptions.UserNotFoundException;
import ar.edu.uade.ecommerce.Exceptions.AccountNotVerifiedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthService authService;
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // No es necesario inicializar manualmente con MockitoAnnotations.openMocks al usar MockitoExtension
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
    void testLogin_InvalidCredentials() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "fail@email.com");
        request.put("password", "wrongpass");
        when(authService.login("fail@email.com", "wrongpass")).thenThrow(new InvalidCredentialsException("Credenciales inválidas"));
        assertThrows(InvalidCredentialsException.class, () -> authController.login(request));
    }

    @Test
    void testLogin_UserNotFound() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "noexiste@email.com");
        request.put("password", "cualquier");
        when(authService.login("noexiste@email.com", "cualquier")).thenThrow(new UserNotFoundException("Usuario no encontrado"));
        assertThrows(UserNotFoundException.class, () -> authController.login(request));
    }

    @Test
    void testLogin_AccountNotVerified() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "pending@email.com");
        request.put("password", "pass");
        when(authService.login("pending@email.com", "pass")).thenThrow(new AccountNotVerifiedException("La cuenta no está verificada. Por favor verifica tu correo electrónico."));
        assertThrows(AccountNotVerifiedException.class, () -> authController.login(request));
    }

    @Test
    void testRegister_NewUserCreated201() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setEmail("new@email.com");
        dto.setPassword("pass");
        dto.setName("Nuevo");
        dto.setLastname("Usuario");
        User newUser = new User();
        newUser.setId(2);
        newUser.setName("Nuevo");
        newUser.setLastname("Usuario");
        newUser.setEmail("new@email.com");
        newUser.setRole("USER");
        when(authService.getUserByEmail("new@email.com")).thenReturn(null);
        when(authService.registerDTO(dto)).thenReturn(newUser);
        ResponseEntity<?> response = authController.register(dto);
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertInstanceOf(Map.class, response.getBody());
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertTrue(body.get("message").toString().contains("Fuiste registrado satisfactoriamente"));
        assertNotNull(body.get("user"));
    }

    @Test
    void testRegister_ExistingActiveUserThrows409() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setEmail("exist@email.com");
        dto.setPassword("pass");
        dto.setName("Existente");
        User existingUser = new User();
        existingUser.setEmail("exist@email.com");
        existingUser.setAccountActive(true);
        when(authService.getUserByEmail("exist@email.com")).thenReturn(existingUser);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> authController.register(dto));
        assertTrue(ex.getMessage().contains("La cuenta ya está activa"));
    }

    @Test
    void testRegister_ExistingInactiveUserResendsToken() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setEmail("inactive@email.com");
        dto.setPassword("pass");
        dto.setName("Inactivo");
        User existingUser = new User();
        existingUser.setEmail("inactive@email.com");
        existingUser.setAccountActive(false);
        when(authService.getUserByEmail("inactive@email.com")).thenReturn(existingUser);
        doNothing().when(authService).resendVerificationToken(existingUser);
        ResponseEntity<?> response = authController.register(dto);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertInstanceOf(String.class, response.getBody());
        assertTrue(response.getBody().toString().contains("La cuenta ya existe pero no está activa"));
    }

    @Test
    void testRegister_InvalidName_AllDigits() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setEmail("digits@email.com");
        dto.setPassword("pass");
        dto.setName("123456");
        ResponseEntity<?> response = authController.register(dto);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertInstanceOf(String.class, response.getBody());
        assertTrue(response.getBody().toString().contains("El nombre debe contener al menos una letra"));
    }

    @Test
    void testRegister_IncompleteData() {
        RegisterUserDTO dto = new RegisterUserDTO(); // sin email ni password
        ResponseEntity<?> response = authController.register(dto);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertInstanceOf(String.class, response.getBody());
        assertTrue(response.getBody().toString().contains("Datos incompletos"));
    }

//    @Test
//    void testVerifyEmail_Success() {
//        Map<String, String> request = new HashMap<>();
//        request.put("email", "test@email.com");
//        request.put("token", "token123");
//        when(authService.verifyEmailToken("test@email.com", "token123")).thenReturn(true);
//        ResponseEntity<String> response = authController.verifyEmail(request);
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        assertTrue(response.getBody().contains("Cuenta verificada exitosamente"));
//    }

//    @Test
//    void testVerifyEmail_Failure() {
//        Map<String, String> request = new HashMap<>();
//        request.put("email", "test@email.com");
//        request.put("token", "token123");
//        when(authService.verifyEmailToken("test@email.com", "token123")).thenReturn(false);
//        ResponseEntity<String> response = authController.verifyEmail(request);
//        assertEquals(400, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        assertTrue(response.getBody().contains("Token inválido o expirado"));
//    }

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

    @Test
    void testLogout_InvalidHeader() {
        ResponseEntity<String> response = authController.logout("Token malo");
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testLogin_BadRequestMissingFields() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "soloEmail@email.com"); // sin password
        ResponseEntity<UserLoginResponseDTO> response = authController.login(request);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }
}
