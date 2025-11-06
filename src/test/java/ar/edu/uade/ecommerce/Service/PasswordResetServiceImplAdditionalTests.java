package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Token;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.TokenRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplAdditionalTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User user;
    private Token token;

    @BeforeEach
    void setUp() {
        user = new User(); user.setId(7); user.setEmail("u@test.com");
        token = new Token(); token.setToken("tok123"); token.setUser(user); token.setExpirationDate(new Date(System.currentTimeMillis()+10000));
    }

    @Test
    void requestPasswordReset_userNotFound_throws() {
        when(userRepository.findByEmail("no@ex.com")).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> passwordResetService.requestPasswordReset("no@ex.com"));
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
    }

    @Test
    void validateToken_returnsTrueForValid() {
        when(userRepository.findByEmail("u@test.com")).thenReturn(user);
        when(tokenRepository.findByToken("tok123")).thenReturn(Optional.of(token));
        assertTrue(passwordResetService.validateToken("u@test.com", "tok123"));
    }

    @Test
    void validateToken_returnsFalseForExpired() {
        token.setExpirationDate(new Date(System.currentTimeMillis()-10000));
        when(userRepository.findByEmail("u@test.com")).thenReturn(user);
        when(tokenRepository.findByToken("tok123")).thenReturn(Optional.of(token));
        assertFalse(passwordResetService.validateToken("u@test.com", "tok123"));
    }

    @Test
    void changePassword_successfulFlow() {
        when(passwordEncoder.encode("newpass")).thenReturn("hashedNew");
        when(userRepository.findByEmail("u@test.com")).thenReturn(user);
        when(tokenRepository.findByToken("tok123")).thenReturn(Optional.of(token));

        assertDoesNotThrow(() -> passwordResetService.changePassword("u@test.com", "tok123", "newpass"));
        verify(userRepository).save(user);
        verify(tokenRepository).deleteByToken("tok123");
    }

    @Test
    void changePassword_invalidNewPassword_throws() {
        assertThrows(IllegalArgumentException.class, () -> passwordResetService.changePassword("u@test.com", "tok123", "  "));
    }
}
