package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import ar.edu.uade.ecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null) {
            return ResponseEntity.status(401).body(null);
        }
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).body(null);
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/address")
    public ResponseEntity<?> addAddress(@RequestHeader("Authorization") String authHeader, @RequestBody Address address) {
        if (authHeader == null) {
            return ResponseEntity.status(401).body("Usuario no logueado");
        }
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).body("Usuario no logueado");
        }
        if (address == null) {
            return ResponseEntity.ok(null);
        }
        // Validación extra: si la dirección ya existe para el usuario, retorna 409
        if (user.getAddresses() != null) {
            for (Object addr : user.getAddresses()) {
                if (addr.equals(address)) {
                    return ResponseEntity.status(409).body("La dirección ya existe para el usuario");
                }
            }
        }
        address.setUser(user);
        Address saved = userService.addAddress(address);
        return ResponseEntity.ok(saved); // Devuelve el JSON de la dirección creada
    }

    @PutMapping("/address/{id}")
    public ResponseEntity<?> updateAddress(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id, @RequestBody Address address) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).body("Usuario no logueado");
        }
        address.setUser(user);
        Address updated = userService.updateAddress(id, address);
        return ResponseEntity.ok(updated); // Devuelve el JSON de la dirección modificada
    }

    @DeleteMapping("/address/{id}")
    public ResponseEntity<?> deleteAddress(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).body("Usuario no logueado");
        }
        userService.deleteAddress(id, user);
        return ResponseEntity.ok("Dirección eliminada con éxito"); // Devuelve mensaje de éxito
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateUserData(@RequestHeader("Authorization") String authHeader, @RequestBody User userPatch) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).body("Usuario no logueado");
        }
        // Solo se pueden modificar name, lastname, password
        if (userPatch.getName() != null) user.setName(userPatch.getName());
        if (userPatch.getLastname() != null) user.setLastname(userPatch.getLastname());
        if (userPatch.getPassword() != null && !userPatch.getPassword().isEmpty()) user.setPassword(userPatch.getPassword());
        User updated = userService.saveUser(user);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<Address>> getUserAddresses(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).body(null);
        }
        return ResponseEntity.ok((List<Address>) user.getAddresses());
    }


}
