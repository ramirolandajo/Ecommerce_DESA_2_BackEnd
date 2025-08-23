package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.User;

public interface AuthService {
    String login(String email, String password);
    User register(User user);
    User registerDTO(ar.edu.uade.ecommerce.Entity.DTO.RegisterUserDTO registerUserDTO);
    String getEmailFromToken(String token);
    User getUserByEmail(String email);
    boolean verifyEmailToken(String email, String token);
}
