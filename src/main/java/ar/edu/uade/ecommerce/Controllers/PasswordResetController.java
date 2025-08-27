package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/password")
public class PasswordResetController {
    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        passwordResetService.requestPasswordReset(email);
        return ResponseEntity.ok("Se ha enviado un correo con el código de recuperación");
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String token = body.get("token");
        boolean valid = passwordResetService.validateToken(email, token);
        if (valid) {
            return ResponseEntity.ok("Token válido");
        } else {
            return ResponseEntity.badRequest().body("Token inválido o expirado");
        }
    }

    @PostMapping("/change")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        passwordResetService.changePassword(email, token, newPassword);
        return ResponseEntity.ok("Contraseña cambiada exitosamente");
    }
}

