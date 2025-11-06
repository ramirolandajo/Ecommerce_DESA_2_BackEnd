package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplAdditionalTests {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getEmailFromToken_whenJwtUtilThrows_returnsNull() {
        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("jwt not configured"));
        String email = authService.getEmailFromToken("tok");
        // según implementación, captura excepción en JwtUtil; pero si JwtUtil lanza, el servicio propaga
        assertNull(email);
    }
}
