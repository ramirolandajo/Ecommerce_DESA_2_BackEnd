package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BackendTokenManagerExpiryEdgeTests {

    static class StubKeycloak extends KeycloakClient {
        TokenInfo next;
        @Override public synchronized TokenInfo getClientAccessTokenInfo() { return next; }
    }

    @Test
    void getToken_refreshes_whenWithinRefreshWindow() {
        StubKeycloak kc = new StubKeycloak();
        kc.next = new KeycloakClient.TokenInfo("NEW", 120);
        BackendTokenManager mgr = new BackendTokenManager(kc);
        org.springframework.test.util.ReflectionTestUtils.setField(mgr, "token", "OLD");
        // expiry soon (10s), refreshBeforeSeconds=30 => debe refrescar
        org.springframework.test.util.ReflectionTestUtils.setField(mgr, "expiry", Instant.now().plusSeconds(10));
        String t = mgr.getToken();
        assertEquals("NEW", t);
    }
}

