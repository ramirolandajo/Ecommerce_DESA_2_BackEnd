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
public class AuthServiceImplTest {
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
    void testLogin_success() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword("hashed");
        user.setAccountActive(true);
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(passwordEncoder.matches("1234", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("test@mail.com")).thenReturn("token");
        String result = authService.login("test@mail.com", "1234");
        assertEquals("token", result);
    }

    @Test
    void testLogin_invalidCredentials() {
        when(userRepository.findByEmail("test@mail.com")).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login("test@mail.com", "1234"));
        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    void testLogin_notVerified() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword("hashed");
        user.setAccountActive(false);
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(passwordEncoder.matches("1234", "hashed")).thenReturn(true);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login("test@mail.com", "1234"));
        assertEquals("La cuenta no está verificada. Por favor verifica tu correo electrónico.", ex.getMessage());
    }

    @Test
    void testLogin_wrongPassword() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword("hashed");
        user.setAccountActive(true);
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login("test@mail.com", "wrongpass"));
        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    void testRegisterDTO_success() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setName("Juan");
        dto.setLastname("Perez");
        dto.setEmail("test@mail.com");
        dto.setPassword("1234");
        User user = new User();
        user.setId(1);
        user.setEmail("test@mail.com");
        when(passwordEncoder.encode("1234")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenRepository.save(any(Token.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        User result = authService.registerDTO(dto);
        assertEquals(user, result);
        verify(tokenRepository).save(any(Token.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testVerifyEmailToken_success() {
        User user = new User(); user.setId(1); user.setEmail("test@mail.com");
        Token token = new Token(); token.setUser(user); token.setToken("abc"); token.setExpirationDate(new Date(System.currentTimeMillis() + 10000));
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(tokenRepository).deleteByUserId(1);
        boolean result = authService.verifyEmailToken("test@mail.com", "abc");
        assertTrue(result);
        verify(userRepository).save(user);
        verify(tokenRepository).deleteByUserId(1);
    }

    @Test
    void testVerifyEmailToken_userNotFound() {
        when(userRepository.findByEmail("notfound@mail.com")).thenReturn(null);
        boolean result = authService.verifyEmailToken("notfound@mail.com", "abc");
        assertFalse(result);
    }

    @Test
    void testVerifyEmailToken_tokenNotFound() {
        User user = new User(); user.setId(1);
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.empty());
        boolean result = authService.verifyEmailToken("test@mail.com", "abc");
        assertFalse(result);
    }

    @Test
    void testVerifyEmailToken_tokenNotBelongsToUser() {
        User user = new User(); user.setId(1);
        User other = new User(); other.setId(2);
        Token token = new Token(); token.setUser(other); token.setToken("abc"); token.setExpirationDate(new Date(System.currentTimeMillis() + 10000));
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        boolean result = authService.verifyEmailToken("test@mail.com", "abc");
        assertFalse(result);
    }

    @Test
    void testVerifyEmailToken_tokenExpired() {
        User user = new User(); user.setId(1);
        Token token = new Token(); token.setUser(user); token.setToken("abc"); token.setExpirationDate(new Date(System.currentTimeMillis() - 10000));
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        doNothing().when(tokenRepository).deleteByToken("abc");
        boolean result = authService.verifyEmailToken("test@mail.com", "abc");
        assertFalse(result);
        verify(tokenRepository).deleteByToken("abc");
    }

    @Test
    void testResendVerificationToken() {
        User user = new User(); user.setId(1); user.setEmail("test@mail.com");
        when(tokenRepository.save(any(Token.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        authService.resendVerificationToken(user);
        verify(tokenRepository).save(any(Token.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testGenerateToken() {
        String token = authService.generateToken();
        assertNotNull(token);
        assertEquals(8, token.length());
        assertTrue(token.matches("[A-Za-z0-9]+"));
    }

    @Test
    void testSendVerificationEmail() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> authService.sendVerificationEmail("test@mail.com", "token123"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testRegister() {
        User user = new User();
        user.setPassword("1234");
        when(passwordEncoder.encode("1234")).thenReturn("hashed");
        when(userRepository.save(user)).thenReturn(user);
        User result = authService.register(user);
        assertEquals(user, result);
        assertEquals("hashed", user.getPassword());
    }

    @Test
    void testSaveUser() {
        User user = new User();
        authService.saveUser(user);
        verify(userRepository).save(user);
    }

    @Test
    void testGetEmailFromToken() {
        when(jwtUtil.extractUsername("token")).thenReturn("test@mail.com");
        String email = authService.getEmailFromToken("token");
        assertEquals("test@mail.com", email);
    }

    @Test
    void testGetUserByEmail() {
        User user = new User();
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        User result = authService.getUserByEmail("test@mail.com");
        assertEquals(user, result);
    }
}
