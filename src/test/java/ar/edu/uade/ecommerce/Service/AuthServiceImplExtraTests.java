package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Entity.Token;
import ar.edu.uade.ecommerce.Entity.DTO.RegisterUserDTO;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.Repository.TokenRepository;
import ar.edu.uade.ecommerce.Security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Date;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplExtraTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private JavaMailSender mailSender;
    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testLogin_whenPasswordEncoderThrowsException() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword("hashed");
        user.setAccountActive(true);
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(passwordEncoder.matches("1234", "hashed")).thenThrow(new RuntimeException("Encoder error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login("test@mail.com", "1234"));
        assertEquals("Encoder error", ex.getMessage());
    }

    @Test
    void testRegisterDTO_whenUserRepositorySaveThrowsException() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setName("Juan");
        dto.setLastname("Perez");
        dto.setEmail("test@mail.com");
        dto.setPassword("1234");
        when(passwordEncoder.encode("1234")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Save failed"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.registerDTO(dto));
        assertEquals("Save failed", ex.getMessage());
    }

    @Test
    void testVerifyEmailToken_whenTokenRepositoryDeleteThrowsException() {
        User user = new User(); user.setId(1); user.setEmail("test@mail.com");
        Token token = new Token(); token.setUser(user); token.setToken("abc"); token.setExpirationDate(new Date(System.currentTimeMillis() + 10000));
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(userRepository.save(user)).thenReturn(user);
        doThrow(new RuntimeException("Delete failed")).when(tokenRepository).deleteByUserId(1);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.verifyEmailToken("test@mail.com", "abc"));
        assertEquals("Delete failed", ex.getMessage());
    }

    @Test
    void testResendVerificationToken_whenMailSenderThrowsException() {
        User user = new User(); user.setId(1); user.setEmail("test@mail.com");
        when(tokenRepository.save(any(Token.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("Mail failed")).when(mailSender).send(any(SimpleMailMessage.class));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.resendVerificationToken(user));
        assertEquals("Mail failed", ex.getMessage());
    }

    @Test
    void testSendVerificationEmail_whenMailSenderThrowsException() {
        doThrow(new RuntimeException("Mail send error")).when(mailSender).send(any(SimpleMailMessage.class));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.sendVerificationEmail("test@mail.com", "token123"));
        assertEquals("Mail send error", ex.getMessage());
    }

    @Test
    void testGetEmailFromToken_whenJwtUtilThrows_returnsNull() {
        when(jwtUtil.extractUsername("token")).thenThrow(new RuntimeException("JWT error"));
        assertNull(authService.getEmailFromToken("token"));
    }

    @Test
    void testGetUserByEmail_whenRepositoryReturnsNull() {
        when(userRepository.findByEmail("notfound@mail.com")).thenReturn(null);
        User result = authService.getUserByEmail("notfound@mail.com");
        assertNull(result);
    }
}
