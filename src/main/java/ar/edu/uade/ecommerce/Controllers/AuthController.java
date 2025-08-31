package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.RegisterUserDTO;
import ar.edu.uade.ecommerce.Entity.DTO.UserLoginResponseDTO;
import ar.edu.uade.ecommerce.Entity.DTO.UserResponseDTO;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String token = authService.login(email, password);
        User user = authService.getUserByEmail(email);
        if (user == null) {
            UserLoginResponseDTO response = new UserLoginResponseDTO();
            response.setSuccess(false);
            response.setBearer_token(null);
            response.setUser(null);
            return ResponseEntity.ok(response);
        }
        user.setSessionActive(true);
        authService.saveUser(user);
        UserLoginResponseDTO.UserBasicDTO userDTO = new UserLoginResponseDTO.UserBasicDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setLastname(user.getLastname());
        userDTO.setEmail(user.getEmail());
        userDTO.setAddresses(user.getAddresses());
        UserLoginResponseDTO response = new UserLoginResponseDTO();
        response.setSuccess(token != null);
        response.setBearer_token(token);
        response.setUser(userDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterUserDTO registerUserDTO) {
        User existingUser = authService.getUserByEmail(registerUserDTO.getEmail());
        if (existingUser != null) {
            if (existingUser.isAccountActive()) {
                return ResponseEntity.badRequest().body("La cuenta ya está activa. Inicia sesión.");
            } else {
                // Reenvía el token de verificación
                authService.resendVerificationToken(existingUser);
                return ResponseEntity.ok("La cuenta ya existe pero no está activa. Se ha reenviado el token de verificación a tu correo.");
            }
        }
        User newUser = authService.registerDTO(registerUserDTO);
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(newUser.getId());
        responseDTO.setName(newUser.getName());
        responseDTO.setLastname(newUser.getLastname());
        responseDTO.setEmail(newUser.getEmail());
        newUser.setRole("USER");
        return ResponseEntity.ok("Fuiste registrado satisfactoriamente, revisa tu casilla de correo para ingresar luego el token para activar la cuenta");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String token = request.get("token");
        boolean verified = authService.verifyEmailToken(email, token);
        if (verified) {
            return ResponseEntity.ok("Cuenta verificada exitosamente. Ya puedes iniciar sesión.");
        } else {
            return ResponseEntity.badRequest().body("Token inválido o expirado");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        user.setSessionActive(false);
        authService.saveUser(user);
        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}
