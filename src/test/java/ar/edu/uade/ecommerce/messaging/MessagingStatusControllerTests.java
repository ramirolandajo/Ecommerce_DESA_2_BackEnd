package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessagingStatusControllerTests {

    @Test
    void status_populatesMapWithConfigAndRuntime() {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        KeycloakClient kc = mock(KeycloakClient.class);
        MessagingStatusController ctrl = new MessagingStatusController(mgr, kc);
        // inyectar configuredBackendToken por reflexión para simular property
        org.springframework.test.util.ReflectionTestUtils.setField(ctrl, "configuredBackendToken", "abcdef123456");
        when(mgr.getToken()).thenReturn("zzz999");
        when(kc.getClientAccessTokenInfo()).thenReturn(new KeycloakClient.TokenInfo("abc123token", 120));
        Map<String,Object> out = ctrl.status();
        assertTrue((Boolean) out.get("configuredBackendTokenPresent"));
        assertTrue((Boolean) out.get("backendTokenCached"));
        assertTrue((Boolean) out.get("keycloakClientAvailable"));
        assertEquals("abcdef12...", out.get("configuredBackendTokenPreview"));
        assertEquals("zzz999", out.get("backendTokenPreview"));
        assertEquals("abc123to...", out.get("keycloakAccessTokenPreview"));
        assertEquals(120L, out.get("keycloakAccessTokenExpiresIn"));
    }
}

