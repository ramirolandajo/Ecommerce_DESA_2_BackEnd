package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Service.PasswordResetService;
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
class PasswordResetControllerTest {
    @Mock
    private PasswordResetService passwordResetService;
    @InjectMocks
    private PasswordResetController passwordResetController;

    private Map<String, String> m(String... entries) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put(entries[i], entries[i + 1]);
        }
        return map;
    }

    @Test
    void testRequestPasswordReset() {
        doNothing().when(passwordResetService).requestPasswordReset("test@email.com");
        ResponseEntity<String> response = passwordResetController.requestPasswordReset(m("email", "test@email.com"));
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Se ha enviado un correo con el código de recuperación", response.getBody());
    }

    @Test
    void testValidateToken_Valid() {
        when(passwordResetService.validateToken("test@email.com", "token123")).thenReturn(true);
        ResponseEntity<String> response = passwordResetController.validateToken(m("email", "test@email.com", "token", "token123"));
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Token válido", response.getBody());
    }

    @Test
    void testValidateToken_Invalid() {
        when(passwordResetService.validateToken("test@email.com", "token123")).thenReturn(false);
        ResponseEntity<String> response = passwordResetController.validateToken(m("email", "test@email.com", "token", "token123"));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Token inválido o expirado", response.getBody());
    }

    @Test
    void testChangePassword() {
        doNothing().when(passwordResetService).changePassword("test@email.com", "token123", "newpass");
        ResponseEntity<String> response = passwordResetController.changePassword(m("email", "test@email.com", "token", "token123", "newPassword", "newpass"));
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Contraseña cambiada exitosamente", response.getBody());
    }
}
