package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetControllerTest {
    @Mock
    private PasswordResetService passwordResetService;
    @InjectMocks
    private PasswordResetController passwordResetController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRequestPasswordReset() {
        doNothing().when(passwordResetService).requestPasswordReset("test@email.com");
        ResponseEntity<String> response = passwordResetController.requestPasswordReset(Map.of("email", "test@email.com"));
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Se ha enviado un correo con el código de recuperación", response.getBody());
    }

    @Test
    void testValidateToken_Valid() {
        when(passwordResetService.validateToken("test@email.com", "token123")).thenReturn(true);
        ResponseEntity<String> response = passwordResetController.validateToken(Map.of("email", "test@email.com", "token", "token123"));
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Token válido", response.getBody());
    }

    @Test
    void testValidateToken_Invalid() {
        when(passwordResetService.validateToken("test@email.com", "token123")).thenReturn(false);
        ResponseEntity<String> response = passwordResetController.validateToken(Map.of("email", "test@email.com", "token", "token123"));
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Token inválido o expirado", response.getBody());
    }

    @Test
    void testChangePassword() {
        doNothing().when(passwordResetService).changePassword("test@email.com", "token123", "newpass");
        ResponseEntity<String> response = passwordResetController.changePassword(Map.of("email", "test@email.com", "token", "token123", "newPassword", "newpass"));
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Contraseña cambiada exitosamente", response.getBody());
    }
}

