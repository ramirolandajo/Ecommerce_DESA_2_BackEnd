package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.RegisterUserDTO;
import ar.edu.uade.ecommerce.Entity.DTO.UserLoginResponseDTO;
import ar.edu.uade.ecommerce.Entity.DTO.UserResponseDTO;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody Map<String, String> request) {
        if (request == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failLogin());
        }
        String email = request.get("email");
        String password = request.get("password");
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failLogin());
        }
        String token = authService.login(email, password); // puede lanzar excepciones específicas
        User user = authService.getUserByEmail(email);
        UserLoginResponseDTO.UserBasicDTO userDTO = new UserLoginResponseDTO.UserBasicDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setLastname(user.getLastname());
        userDTO.setEmail(user.getEmail());
        userDTO.setAddresses(user.getAddresses());
        user.setSessionActive(true);
        authService.saveUser(user);
        UserLoginResponseDTO response = new UserLoginResponseDTO();
        response.setSuccess(true);
        response.setBearer_token(token);
        response.setUser(userDTO);
        return ResponseEntity.ok(response);
    }

    private UserLoginResponseDTO failLogin() {
        UserLoginResponseDTO resp = new UserLoginResponseDTO();
        resp.setSuccess(false);
        resp.setBearer_token(null);
        resp.setUser(null);
        return resp;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserDTO registerUserDTO) {
        if (registerUserDTO == null || registerUserDTO.getEmail() == null || registerUserDTO.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Datos incompletos para el registro");
        }
        // Validación: nombre debe contener al menos una letra (A-Z o a-z)
        String name = registerUserDTO.getName();
        if (name == null || !name.matches(".*[A-Za-zÁÉÍÓÚáéíóúÑñ].*")) {
            return ResponseEntity
                    .badRequest()
                    .body(java.util.Map.of("error", "El nombre debe contener al menos una letra"));
        }
        User existingUser = authService.getUserByEmail(registerUserDTO.getEmail());
        if (existingUser != null) {
            if (existingUser.isAccountActive()) {
                // Usar IllegalStateException para que el handler global devuelva 409
                throw new IllegalStateException("La cuenta ya está activa. Inicia sesión.");
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
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Fuiste registrado satisfactoriamente, revisa tu casilla de correo para ingresar luego el token para activar la cuenta");
        body.put("user", responseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody Map<String, String> request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Solicitud inválida"));
        }
        String email = request.get("email");
        String token = request.get("token");
        if (email == null || token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email o token faltante"));
        }
        try {
            boolean verified = authService.verifyEmailToken(email, token);
            if (verified) {
                return ResponseEntity.ok(Map.of("message", "Cuenta verificada exitosamente. Ya puedes iniciar sesión."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El token ingresado no es el correcto, intentelo nuevamente"));
            }
        } catch (Exception ex) {
            logger.warn("Error verificando token para {}: {}", email, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El token ingresado no es el correcto, intentelo nuevamente"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cabecera Authorization inválida");
        }
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }
        User user = authService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
        user.setSessionActive(false);
        authService.saveUser(user);
        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}
