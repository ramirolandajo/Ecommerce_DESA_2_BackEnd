package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.UserResponseDTO;
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

}
