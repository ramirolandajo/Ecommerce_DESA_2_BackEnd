package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BackendTokenManagerRefreshTests {

    static class FakeKeycloakClient extends KeycloakClient {
        TokenInfo next;
        @Override
        public synchronized TokenInfo getClientAccessTokenInfo() {
            return next;
        }
    }

    @Test
    void getToken_returnsCachedWhenExceptionAndValid() {
        FakeKeycloakClient kc = new FakeKeycloakClient();
        kc.next = new KeycloakClient.TokenInfo("abc", 300);
        BackendTokenManager mgr = new BackendTokenManager(kc);
        // primera vez obtiene y cachea
        assertEquals("abc", mgr.getToken());
        // simular que no hay nuevo token, pero cache sigue válido
        kc.next = null;
        assertEquals("abc", mgr.getToken());
    }

    @Test
    void refreshIfNearExpiry_runsWhenEnabled() {
        FakeKeycloakClient kc = new FakeKeycloakClient();
        kc.next = new KeycloakClient.TokenInfo("tok1", 1);
        BackendTokenManager mgr = new BackendTokenManager(kc);
        assertEquals("tok1", mgr.getToken());
        // activar refresh y forzar near expiry
        org.springframework.test.util.ReflectionTestUtils.setField(mgr, "refreshEnabled", true);
        org.springframework.test.util.ReflectionTestUtils.setField(mgr, "expiry", Instant.now());
        // siguiente llamada intenta refrescar
        kc.next = new KeycloakClient.TokenInfo("tok2", 120);
        assertDoesNotThrow(mgr::refreshIfNearExpiry);
        // getToken debería devolver el cache actualizado
        assertEquals("tok2", mgr.getToken());
    }
}

