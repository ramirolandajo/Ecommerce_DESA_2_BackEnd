package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.RegisterUserDTO;
import ar.edu.uade.ecommerce.Entity.DTO.UserResponseDTO;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        String token = authService.login(email, password);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody RegisterUserDTO registerUserDTO) {
        User newUser = authService.registerDTO(registerUserDTO);
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(newUser.getId());
        responseDTO.setName(newUser.getName());
        responseDTO.setLastname(newUser.getLastname());
        responseDTO.setEmail(newUser.getEmail());
        responseDTO.setRole(newUser.getRole());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(user.getId());
        responseDTO.setName(user.getName());
        responseDTO.setLastname(user.getLastname());
        responseDTO.setEmail(user.getEmail());
        responseDTO.setRole(user.getRole());
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String token) {
        boolean verified = authService.verifyEmailToken(email, token);
        if (verified) {
            return ResponseEntity.ok("Cuenta verificada exitosamente. Ya puedes iniciar sesión.");
        } else {
            return ResponseEntity.badRequest().body("Token inválido o expirado");
        }
    }
}
