package ar.edu.uade.ecommerce.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {
    @Value("${jwt.secret:}")
    private String secret;

    private Key signingKey;
    private boolean enabled = false;

    private final long EXPIRATION_TIME = 7200000; // 2 horas

    @PostConstruct
    public void init() {
        try {
            if (secret != null && !secret.isBlank()) {
                // key length requirements depend on algorithm; HS512 requires a sufficiently long key
                signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                enabled = true;
            } else {
                enabled = false;
            }
        } catch (Exception ex) {
            // If key is invalid/too short, disable JWT operations to avoid breaking app startup
            enabled = false;
        }
    }

    // Nuevo método: inicialización perezosa para soportar tests que establecen el campo `secret`
    private synchronized void ensureInitialized() {
        if (enabled) return;
        if (secret != null && !secret.isBlank()) {
            try {
                signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                enabled = true;
            } catch (Exception ex) {
                enabled = false;
            }
        }
    }

    public String generateToken(String username) {
        // Soportar inicialización perezosa en entornos de test donde @PostConstruct no se ejecutó
        ensureInitialized();
        if (!enabled) throw new IllegalStateException("JWT is not configured (jwt.secret missing or invalid)");
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            Claims claims = getClaims(token);
            return claims != null ? claims.getSubject() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token, String username) {
        // permitir inicializar si es necesario
        ensureInitialized();
        if (!enabled) return false;
        String extractedUsername = extractUsername(token);
        if (extractedUsername == null || username == null) return false;
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Claims claims = getClaims(token);
        if (claims == null || claims.getExpiration() == null) return true;
        return claims.getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        // asegurar inicialización también aquí
        ensureInitialized();
        if (!enabled) return null;
        try {
            return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
        } catch (Exception ex) {
            return null;
        }
    }

    // Nuevo: exponer si JWT está habilitado
    public boolean isEnabled() {
        return enabled;
    }

    // Nuevo: preview del secret para debugging (no mostrar completo)
    public String getSecretPreview() {
        if (secret == null) return null;
        String s = secret.trim();
        if (s.length() <= 8) return s;
        return s.substring(0,8) + "...";
    }
}
