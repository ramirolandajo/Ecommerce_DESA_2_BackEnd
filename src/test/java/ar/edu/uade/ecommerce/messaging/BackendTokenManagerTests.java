package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BackendTokenManagerTests {

    static class FakeKeycloakClient extends KeycloakClient {
        TokenInfo next;
        @Override
        public synchronized TokenInfo getClientAccessTokenInfo() {
            return next;
        }
    }

    @Test
    void getToken_returnsNullWhenNoInfoAndNoCache() {
        FakeKeycloakClient kc = new FakeKeycloakClient();
        kc.next = null;
        BackendTokenManager mgr = new BackendTokenManager(kc);
        assertNull(mgr.getToken());
    }

    @Test
    void getToken_cachesValue() {
        FakeKeycloakClient kc = new FakeKeycloakClient();
        kc.next = new KeycloakClient.TokenInfo("abc", 300);
        BackendTokenManager mgr = new BackendTokenManager(kc);
        assertEquals("abc", mgr.getToken());
        // siguiente llamada con next null devuelve token cacheado
        kc.next = null;
        assertEquals("abc", mgr.getToken());
    }
}

