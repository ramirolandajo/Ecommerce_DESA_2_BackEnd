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
        try {
            if (request == null) {
                UserLoginResponseDTO resp = new UserLoginResponseDTO();
                resp.setSuccess(false);
                resp.setBearer_token(null);
                resp.setUser(null);
                return ResponseEntity.badRequest().body(resp);
            }
            String email = request.get("email");
            String password = request.get("password");
            if (email == null || password == null || email.isBlank() || password.isBlank()) {
                UserLoginResponseDTO resp = new UserLoginResponseDTO();
                resp.setSuccess(false);
                resp.setBearer_token(null);
                resp.setUser(null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }

            String token = authService.login(email, password);
            User user = authService.getUserByEmail(email);
            if (user == null) {
                UserLoginResponseDTO resp = new UserLoginResponseDTO();
                resp.setSuccess(false);
                resp.setBearer_token(null);
                resp.setUser(null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }

            if (token == null) {
                UserLoginResponseDTO resp = new UserLoginResponseDTO();
                resp.setSuccess(false);
                resp.setBearer_token(null);
                resp.setUser(null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
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
            response.setSuccess(true);
            response.setBearer_token(token);
            response.setUser(userDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error en /auth/login", e);
            UserLoginResponseDTO resp = new UserLoginResponseDTO();
            resp.setSuccess(false);
            resp.setBearer_token(null);
            resp.setUser(null);
            // Mensaje sanitizado para que el frontend no muestre stack traces
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserDTO registerUserDTO) {
        try {
            if (registerUserDTO == null || registerUserDTO.getEmail() == null || registerUserDTO.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Datos incompletos para el registro");
            }
            User existingUser = authService.getUserByEmail(registerUserDTO.getEmail());
            if (existingUser != null) {
                if (existingUser.isAccountActive()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("La cuenta ya está activa. Inicia sesión.");
                } else {
                    // Reenvía el token de verificación
                    authService.resendVerificationToken(existingUser);
                    return ResponseEntity.ok("La cuenta ya existe pero no está activa. Se ha reenviado el token de verificación a tu correo.");
                }
            }
            User newUser = authService.registerDTO(registerUserDTO);
            if (newUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se pudo crear el usuario");
            }
            UserResponseDTO responseDTO = new UserResponseDTO();
            responseDTO.setId(newUser.getId());
            responseDTO.setName(newUser.getName());
            responseDTO.setLastname(newUser.getLastname());
            responseDTO.setEmail(newUser.getEmail());
            newUser.setRole("USER");
            // Devolvemos 201 Created con un mensaje claro
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Fuiste registrado satisfactoriamente, revisa tu casilla de correo para ingresar luego el token para activar la cuenta");
            body.put("user", responseDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (Exception e) {
            logger.error("Error en /auth/register", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error en el servidor. Contacte al administrador.");
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody Map<String, String> request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body("Solicitud inválida");
            }
            String email = request.get("email");
            String token = request.get("token");
            if (email == null || token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email o token faltante");
            }
            boolean verified = authService.verifyEmailToken(email, token);
            if (verified) {
                return ResponseEntity.ok("Cuenta verificada exitosamente. Ya puedes iniciar sesión.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido o expirado");
            }
        } catch (Exception e) {
            logger.error("Error en /auth/verify-email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error en el servidor. Contacte al administrador.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        try {
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
        } catch (Exception e) {
            logger.error("Error en /auth/logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error en el servidor. Contacte al administrador.");
        }
    }
}
