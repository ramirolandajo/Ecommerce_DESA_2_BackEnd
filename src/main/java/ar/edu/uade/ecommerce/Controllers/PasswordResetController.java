package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password")
public class PasswordResetController {
    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        passwordResetService.requestPasswordReset(email);
        return ResponseEntity.ok("Se ha enviado un correo con el código de recuperación");
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestParam String email, @RequestParam String token) {
        boolean valid = passwordResetService.validateToken(email, token);
        if (valid) {
            return ResponseEntity.ok("Token válido");
        } else {
            return ResponseEntity.badRequest().body("Token inválido o expirado");
        }
    }

    @PostMapping("/change")
    public ResponseEntity<String> changePassword(@RequestParam String email, @RequestParam String token, @RequestParam String newPassword) {
        passwordResetService.changePassword(email, token, newPassword);
        return ResponseEntity.ok("Contraseña cambiada exitosamente");
    }
}

