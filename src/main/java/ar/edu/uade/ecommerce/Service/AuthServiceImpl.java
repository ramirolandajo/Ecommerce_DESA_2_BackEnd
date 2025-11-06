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
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
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
        user.setRole("USER");
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
    @Transactional
    public boolean verifyEmailToken(String email, String tokenStr) {
        logger.info("Verificando token {} para el email {}", tokenStr, email);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.warn("Usuario no encontrado para el email: {}", email);
            return false;
        }
        Optional<Token> tokenOpt = tokenRepository.findByToken(tokenStr);
        if (tokenOpt.isEmpty()) {
            logger.warn("Token no encontrado: {}", tokenStr);
            return false;
        }
        Token token = tokenOpt.get();
        if (!token.getUser().getId().equals(user.getId())) {
            logger.warn("El token {} no corresponde al usuario {}", tokenStr, user.getId());
            return false;
        }
        if (token.getExpirationDate().before(new Date())) {
            logger.warn("El token {} está expirado (expiración: {})", tokenStr, token.getExpirationDate());
            tokenRepository.deleteByToken(tokenStr);
            return false;
        }
        // Token válido, activar cuenta y eliminar todos los tokens asociados a ese usuario
        user.setAccountActive(true);
        userRepository.save(user);
        tokenRepository.deleteByUserId(user.getId());
        logger.info("Cuenta activada para el usuario {} y tokens eliminados", user.getId());
        return true;
    }

    String generateToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("CompuMundoHiperMegaRed <no-reply@compumundo.com>");
        message.setTo(to);
        message.setSubject("Verificación de correo electrónico");
        message.setText("Tu código de verificación es: " + token + "\nEste código expira en 15 minutos.");
        mailSender.send(message);
    }

    public String getEmailFromToken(String token) {
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception ex) {
            // En tests y entornos locales puede no estar configurado el JWT
            logger.warn("No se pudo extraer email del token: {}", ex.getMessage());
            return null;
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void resendVerificationToken(User user) {
        String token = generateToken();
        Token verificationToken = new Token();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setExpirationDate(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // 15 minutos
        tokenRepository.save(verificationToken);
        sendVerificationEmail(user.getEmail(), token);
    }

    @Override
    public boolean verifyToken(String token, String email) {
        return false;
    }

    @Override
    public User activateAccount(String token, String email) {
        return null;
    }

    @Override
    public void removeTokensForUser(Integer userId) {

    }
}
