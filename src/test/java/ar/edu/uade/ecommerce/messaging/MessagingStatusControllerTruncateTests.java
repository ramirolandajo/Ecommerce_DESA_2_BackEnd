package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessagingStatusControllerTruncateTests {

    @Test
    void previews_truncateTo8CharsPlusDots() {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        KeycloakClient kc = mock(KeycloakClient.class);
        MessagingStatusController ctrl = new MessagingStatusController(mgr, kc);
        org.springframework.test.util.ReflectionTestUtils.setField(ctrl, "configuredBackendToken", "abcdefghijklmn");
        when(mgr.getToken()).thenReturn("1234567890");
        when(kc.getClientAccessTokenInfo()).thenReturn(new KeycloakClient.TokenInfo("zzzzzzzzzzzz", 60));
        Map<String,Object> out = ctrl.status();
        assertEquals("abcdefgh...", out.get("configuredBackendTokenPreview"));
        assertEquals("12345678...", out.get("backendTokenPreview"));
        assertEquals("zzzzzzzz...", out.get("keycloakAccessTokenPreview"));
        assertEquals(60L, ((Number) out.get("keycloakAccessTokenExpiresIn")).longValue());
    }
}
