package ar.edu.uade.ecommerce.Service;


public interface PasswordResetService {
    void requestPasswordReset(String email);
    boolean validateToken(String email, String token);
    void changePassword(String email, String token, String newPassword);
}

