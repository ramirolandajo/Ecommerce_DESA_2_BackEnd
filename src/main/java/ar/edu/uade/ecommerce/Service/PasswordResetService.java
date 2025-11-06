package ar.edu.uade.ecommerce.Service;


import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetService {
    void requestPasswordReset(String email);

    boolean validateToken(String email, String tokenStr);

    @Transactional
    void changePassword(String email, String tokenStr, String newPassword);
}
