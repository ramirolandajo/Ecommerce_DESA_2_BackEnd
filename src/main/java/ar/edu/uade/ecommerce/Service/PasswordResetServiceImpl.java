package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Token;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.TokenRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int TOKEN_LENGTH = 8;
    private static final long EXPIRATION_MINUTES = 15;

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new RuntimeException("Usuario no encontrado");
        String tokenStr = generateToken();
        Date expiration = new Date(System.currentTimeMillis() + EXPIRATION_MINUTES * 60 * 1000);
        Token token = new Token();
        token.setUser(user);
        token.setToken(tokenStr);
        token.setExpirationDate(expiration);
        tokenRepository.save(token);
        sendEmail(email, tokenStr);
    }

    @Override
    public boolean validateToken(String email, String tokenStr) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;
        Optional<Token> tokenOpt = tokenRepository.findByToken(tokenStr);
        if (tokenOpt.isEmpty()) return false;
        Token token = tokenOpt.get();
        Integer tokenUserId = token.getUser() != null ? token.getUser().getId() : null;
        Integer userId = user.getId();
        if (!java.util.Objects.equals(tokenUserId, userId)) {
            return false;
        }
        Date exp = token.getExpirationDate();
        return exp != null && exp.after(new Date());
    }

    @Transactional
    @Override
    public void changePassword(String email, String tokenStr, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser nula o vacía");
        }
        if (!validateToken(email, tokenStr)) throw new RuntimeException("Token inválido o expirado");
        User user = userRepository.findByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.deleteByToken(tokenStr);
    }

    String generateToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    void sendEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("CompuMundoHiperMegaRed <no-reply@compumundo.com>");
        message.setTo(to);
        message.setSubject("Recuperación de contraseña");
        message.setText("Tu código de recuperación es: " + token + "\nEste código expira en 15 minutos.");
        mailSender.send(message);
    }
}
