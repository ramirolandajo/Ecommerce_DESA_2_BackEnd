package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.User;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    boolean validateToken(String email, String token);
    void changePassword(String email, String token, String newPassword);
}

