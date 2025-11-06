package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Token;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.TokenRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplNewTests {
    @Mock UserRepository userRepository;
    @Mock TokenRepository tokenRepository;
    @Mock JavaMailSender mailSender;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks PasswordResetServiceImpl service;

    @Test
    void requestPasswordReset_persistsTokenAndSendsMail() {
        User u = new User(); u.setEmail("e@e.com");
        when(userRepository.findByEmail("e@e.com")).thenReturn(u);
        when(tokenRepository.save(any(Token.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        service.requestPasswordReset("e@e.com");
        verify(tokenRepository).save(any(Token.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void validateToken_checksUserAndExpiration() {
        User u = new User(); u.setId(1); u.setEmail("e@e.com");
        Token t = new Token(); t.setToken("abc"); t.setUser(u); t.setExpirationDate(new Date(System.currentTimeMillis()+10000));
        when(userRepository.findByEmail("e@e.com")).thenReturn(u);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(t));
        assertTrue(service.validateToken("e@e.com","abc"));
    }

    @Test
    void changePassword_success_deletesToken() {
        User u = new User(); u.setId(1); u.setEmail("e@e.com"); u.setPassword("old");
        Token t = new Token(); t.setToken("abc"); t.setUser(u); t.setExpirationDate(new Date(System.currentTimeMillis()+10000));
        when(userRepository.findByEmail("e@e.com")).thenReturn(u);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(t));
        when(passwordEncoder.encode("new")) .thenReturn("hashed");
        service.changePassword("e@e.com","abc","new");
        verify(userRepository).save(u);
        verify(tokenRepository).deleteByToken("abc");
        assertEquals("hashed", u.getPassword());
    }
}

