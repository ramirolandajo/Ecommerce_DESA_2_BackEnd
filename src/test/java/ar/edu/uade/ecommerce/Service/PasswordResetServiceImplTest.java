package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Token;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.TokenRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
public class PasswordResetServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    @Test
    void testRequestPasswordReset_userExists() {
        User user = new User();
        user.setEmail("test@mail.com");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.save(any(Token.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> passwordResetService.requestPasswordReset("test@mail.com"));
        verify(tokenRepository).save(any(Token.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testRequestPasswordReset_userNotFound() {
        when(userRepository.findByEmail("notfound@mail.com")).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> passwordResetService.requestPasswordReset("notfound@mail.com"));
        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void testValidateToken_valid() {
        User user = new User(); user.setId(1); user.setEmail("test@mail.com");
        Token token = new Token(); token.setUser(user); token.setToken("abc"); token.setExpirationDate(new Date(System.currentTimeMillis() + 10000));
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        assertTrue(passwordResetService.validateToken("test@mail.com", "abc"));
    }

    @Test
    void testValidateToken_userNotFound() {
        when(userRepository.findByEmail("notfound@mail.com")).thenReturn(null);
        assertFalse(passwordResetService.validateToken("notfound@mail.com", "abc"));
    }

    @Test
    void testValidateToken_tokenNotFound() {
        User user = new User(); user.setId(1); user.setEmail("test@mail.com");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.empty());
        assertFalse(passwordResetService.validateToken("test@mail.com", "abc"));
    }

    @Test
    void testValidateToken_tokenExpired() {
        User user = new User(); user.setId(1); user.setEmail("test@mail.com");
        Token token = new Token(); token.setUser(user); token.setToken("abc"); token.setExpirationDate(new Date(System.currentTimeMillis() - 10000));
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        assertFalse(passwordResetService.validateToken("test@mail.com", "abc"));
    }

    @Test
    void testValidateToken_tokenNotBelongsToUser() {
        User user = new User(); user.setId(1); user.setEmail("test@mail.com");
        User other = new User(); other.setId(2);
        Token token = new Token(); token.setUser(other); token.setToken("abc"); token.setExpirationDate(new Date(System.currentTimeMillis() + 10000));
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        assertFalse(passwordResetService.validateToken("test@mail.com", "abc"));
    }

    @Test
    void testChangePassword_success() {
        User user = new User(); user.setId(1); user.setEmail("test@mail.com");
        Token token = new Token(); token.setUser(user); token.setToken("abc"); token.setExpirationDate(new Date(System.currentTimeMillis() + 10000));
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpass")).thenReturn("hashed");
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(tokenRepository).deleteByToken("abc");
        assertDoesNotThrow(() -> passwordResetService.changePassword("test@mail.com", "abc", "newpass"));
        verify(passwordEncoder).encode("newpass");
        verify(userRepository).save(user);
        verify(tokenRepository).deleteByToken("abc");
    }

    @Test
    void testChangePassword_nullPassword() {
        RuntimeException ex = assertThrows(IllegalArgumentException.class, () -> passwordResetService.changePassword("test@mail.com", "abc", null));
        assertEquals("La nueva contraseña no puede ser nula o vacía", ex.getMessage());
    }

    @Test
    void testChangePassword_emptyPassword() {
        RuntimeException ex = assertThrows(IllegalArgumentException.class, () -> passwordResetService.changePassword("test@mail.com", "abc", "   "));
        assertEquals("La nueva contraseña no puede ser nula o vacía", ex.getMessage());
    }

    @Test
    void testChangePassword_invalidToken() {
        User user = new User(); user.setId(1); user.setEmail("test@mail.com");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> passwordResetService.changePassword("test@mail.com", "abc", "newpass"));
        assertEquals("Token inválido o expirado", ex.getMessage());
    }

    @Test
    void testGenerateToken() {
        String token = passwordResetService.generateToken();
        assertNotNull(token);
        assertEquals(8, token.length());
        assertTrue(token.matches("[A-Za-z0-9]+"));
    }

    @Test
    void testSendEmail() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> passwordResetService.sendEmail("test@mail.com", "token123"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
