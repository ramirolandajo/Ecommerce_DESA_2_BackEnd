package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessagingStatusControllerPreviewTests {

    @Test
    void status_tokenPreviewsShortTokensNotTruncated() {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        KeycloakClient kc = mock(KeycloakClient.class);
        MessagingStatusController ctrl = new MessagingStatusController(mgr, kc);
        org.springframework.test.util.ReflectionTestUtils.setField(ctrl, "configuredBackendToken", "abc");
        when(mgr.getToken()).thenReturn("xyz");
        when(kc.getClientAccessTokenInfo()).thenReturn(new KeycloakClient.TokenInfo("short", 10));
        Map<String,Object> out = ctrl.status();
        assertEquals("abc", out.get("configuredBackendTokenPreview"));
        assertEquals("xyz", out.get("backendTokenPreview"));
        assertEquals("short", out.get("keycloakAccessTokenPreview"));
    }
}

