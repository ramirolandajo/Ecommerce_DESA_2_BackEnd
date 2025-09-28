package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BackendTokenManagerTest {

    @Mock
    KeycloakClient keycloakClient;

    @InjectMocks
    BackendTokenManager manager;

    @Test
    public void getToken_cachesTokenAndDoesNotCallKeycloakAgain() {
        KeycloakClient.TokenInfo info = new KeycloakClient.TokenInfo("tokenA", 300);
        when(keycloakClient.getClientAccessTokenInfo()).thenReturn(info);

        String t1 = manager.getToken();
        String t2 = manager.getToken();

        assertEquals("tokenA", t1);
        assertEquals("tokenA", t2);
        // keycloak should be called at most once due to cache
        verify(keycloakClient, atMost(1)).getClientAccessTokenInfo();
    }

    @Test
    public void getToken_refreshesWhenNearExpiry() {
        // First return a token that expires soon (10s)
        KeycloakClient.TokenInfo first = new KeycloakClient.TokenInfo("shortToken", 10);
        KeycloakClient.TokenInfo refreshed = new KeycloakClient.TokenInfo("refreshedToken", 300);
        when(keycloakClient.getClientAccessTokenInfo()).thenReturn(first, refreshed);

        String t1 = manager.getToken();
        // immediate second call should detect token as near-expiry (refreshBeforeSeconds=30) and trigger refresh
        String t2 = manager.getToken();

        assertEquals("shortToken", t1);
        assertEquals("refreshedToken", t2);
        verify(keycloakClient, times(2)).getClientAccessTokenInfo();
    }

    @Test
    public void getToken_returnsNullWhenKeycloakNotConfigured() {
        when(keycloakClient.getClientAccessTokenInfo()).thenReturn(null);
        String t = manager.getToken();
        assertNull(t);
        verify(keycloakClient, times(1)).getClientAccessTokenInfo();
    }
}

