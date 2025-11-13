package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/password")
public class PasswordResetController {
    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        try {
            passwordResetService.requestPasswordReset(email);
            return ResponseEntity.ok(Map.of("message", "Se ha enviado un correo con el código de recuperación"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "El email no pertenece a ninguna cuenta"));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String token = body.get("token");
        boolean valid = passwordResetService.validateToken(email, token);
        if (valid) {
            return ResponseEntity.ok(Map.of("message", "Token válido"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "token incorrecto por favor ingréselo nuevamente"));
        }
    }

    @PostMapping("/change")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        try {
            passwordResetService.changePassword(email, token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Contraseña cambiada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
