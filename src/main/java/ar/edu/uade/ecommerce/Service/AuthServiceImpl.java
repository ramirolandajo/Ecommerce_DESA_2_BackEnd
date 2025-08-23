package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Entity.Token;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.Repository.TokenRepository;
import ar.edu.uade.ecommerce.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            if (!user.isAccountActive()) {
                throw new RuntimeException("La cuenta no está verificada. Por favor verifica tu correo electrónico.");
            }
            return jwtUtil.generateToken(user.getEmail());
        }
        throw new RuntimeException("Credenciales inválidas");
    }

    @Override
    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User registerDTO(ar.edu.uade.ecommerce.Entity.DTO.RegisterUserDTO registerUserDTO) {
        User user = new User();
        user.setName(registerUserDTO.getName());
        user.setLastname(registerUserDTO.getLastname());
        user.setEmail(registerUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerUserDTO.getPassword()));
        user.setRole(registerUserDTO.getRole());
        user.setAccountActive(false);
        User savedUser = userRepository.save(user);
        // Generar y enviar token de verificación
        String tokenStr = generateToken();
        Date expiration = new Date(System.currentTimeMillis() + 15 * 60 * 1000);
        Token token = new Token();
        token.setUser(savedUser);
        token.setToken(tokenStr);
        token.setExpirationDate(expiration);
        tokenRepository.save(token);
        sendVerificationEmail(savedUser.getEmail(), tokenStr);
        return savedUser;
    }

    @Override
    public boolean verifyEmailToken(String email, String tokenStr) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;
        Optional<Token> tokenOpt = tokenRepository.findByToken(tokenStr);
        if (tokenOpt.isEmpty()) return false;
        Token token = tokenOpt.get();
        if (token.getUser().getId().equals(user.getId()) && token.getExpirationDate().after(new Date())) {
            user.setAccountActive(true);
            userRepository.save(user);
            tokenRepository.deleteByToken(tokenStr);
            return true;
        }
        return false;
    }

    private String generateToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("CompuMundoHiperMegaRed <no-reply@compumundo.com>");
        message.setTo(to);
        message.setSubject("Verificación de correo electrónico");
        message.setText("Tu código de verificación es: " + token + "\nEste código expira en 15 minutos.");
        mailSender.send(message);
    }

    public String getEmailFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
