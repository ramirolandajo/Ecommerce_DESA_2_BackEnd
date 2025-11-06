package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakClientUnconfiguredTests {

    @Test
    void getClientAccessToken_returnsNullWhenUnconfigured() {
        KeycloakClient kc = new KeycloakClient();
        assertNull(kc.getClientAccessToken());
    }
}

