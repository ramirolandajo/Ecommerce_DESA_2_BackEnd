package ar.edu.uade.ecommerce.Security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {
    @Test
    void testJwtUtilMethods() throws Exception {
        JwtUtil util = new JwtUtil();
        // Genera una clave segura para HS512
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        java.lang.reflect.Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(util, base64Key);
        String token = util.generateToken("user@email.com");
        assertNotNull(token);
        String username = util.extractUsername(token);
        assertEquals("user@email.com", username);
        assertTrue(util.validateToken(token, "user@email.com"));
        assertFalse(util.validateToken(token, "otro@email.com"));
    }

    @Test
    void testValidateTokenWithNullUsername() throws Exception {
        JwtUtil util = new JwtUtil();
        byte[] keyBytes = io.jsonwebtoken.security.Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512).getEncoded();
        String base64Key = java.util.Base64.getEncoder().encodeToString(keyBytes);
        java.lang.reflect.Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(util, base64Key);
        String token = util.generateToken("user@email.com");
        assertFalse(util.validateToken(token, null));
    }

    @Test
    void testValidateTokenWithInvalidToken() throws Exception {
        JwtUtil util = new JwtUtil();
        byte[] keyBytes = io.jsonwebtoken.security.Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512).getEncoded();
        String base64Key = java.util.Base64.getEncoder().encodeToString(keyBytes);
        java.lang.reflect.Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(util, base64Key);
        // Token inválido
        String invalidToken = "invalid.token.value";
        assertFalse(util.validateToken(invalidToken, "user@email.com"));
    }

    @Test
    void testValidateTokenWithExpiredToken() throws Exception {
        JwtUtil util = new JwtUtil();
        byte[] keyBytes = io.jsonwebtoken.security.Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512).getEncoded();
        String base64Key = java.util.Base64.getEncoder().encodeToString(keyBytes);
        java.lang.reflect.Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(util, base64Key);
        // Genera un token expirado
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("user@email.com")
                .setIssuedAt(new java.util.Date(System.currentTimeMillis() - 10000))
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 5000))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, base64Key)
                .compact();
        assertFalse(util.validateToken(expiredToken, "user@email.com"));
    }

    @Test
    void testValidateTokenWithWrongUsernameAndExpiredToken() throws Exception {
        JwtUtil util = new JwtUtil();
        byte[] keyBytes = io.jsonwebtoken.security.Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512).getEncoded();
        String base64Key = java.util.Base64.getEncoder().encodeToString(keyBytes);
        java.lang.reflect.Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(util, base64Key);
        // Genera un token expirado
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("user@email.com")
                .setIssuedAt(new java.util.Date(System.currentTimeMillis() - 10000))
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 5000))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, base64Key)
                .compact();
        assertFalse(util.validateToken(expiredToken, "otro@email.com"));
    }

    @Test
    void testValidateTokenWithWrongUsernameAndNotExpiredToken() throws Exception {
        JwtUtil util = new JwtUtil();
        byte[] keyBytes = io.jsonwebtoken.security.Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512).getEncoded();
        String base64Key = java.util.Base64.getEncoder().encodeToString(keyBytes);
        java.lang.reflect.Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(util, base64Key);
        // Genera un token válido (no expirado) para user@email.com
        String validToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("user@email.com")
                .setIssuedAt(new java.util.Date(System.currentTimeMillis() - 1000))
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 10000))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, base64Key)
                .compact();
        // username distinto, token no expirado
        assertFalse(util.validateToken(validToken, "otro@email.com"));
    }

    @Test
    void testValidateTokenWithCorrectUsernameAndNotExpiredToken() throws Exception {
        JwtUtil util = new JwtUtil();
        byte[] keyBytes = io.jsonwebtoken.security.Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512).getEncoded();
        String base64Key = java.util.Base64.getEncoder().encodeToString(keyBytes);
        java.lang.reflect.Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(util, base64Key);
        String username = "user@email.com";
        String validToken = io.jsonwebtoken.Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new java.util.Date(System.currentTimeMillis()))
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 10000))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, base64Key)
                .compact();
        assertTrue(util.validateToken(validToken, username));
    }
}
