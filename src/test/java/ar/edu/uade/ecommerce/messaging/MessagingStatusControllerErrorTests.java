package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessagingStatusControllerErrorTests {

    @Test
    void status_handlesExceptionsFromManagers() {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        KeycloakClient kc = mock(KeycloakClient.class);
        MessagingStatusController ctrl = new MessagingStatusController(mgr, kc);
        // configured token vacío
        org.springframework.test.util.ReflectionTestUtils.setField(ctrl, "configuredBackendToken", " ");
        when(mgr.getToken()).thenThrow(new RuntimeException("no token"));
        when(kc.getClientAccessTokenInfo()).thenThrow(new RuntimeException("kc err"));
        Map<String,Object> out = ctrl.status();
        assertFalse((Boolean) out.get("configuredBackendTokenPresent"));
        assertTrue(out.containsKey("backendTokenCachedError"));
        assertTrue(out.containsKey("keycloakClientError"));
    }
}
