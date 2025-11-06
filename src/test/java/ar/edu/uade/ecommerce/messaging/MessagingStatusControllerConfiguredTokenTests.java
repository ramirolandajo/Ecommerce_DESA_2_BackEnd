package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import ar.edu.uade.ecommerce.Security.JwtAuthenticationFilter;
import ar.edu.uade.ecommerce.Security.JwtUtil;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessagingStatusController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MessagingStatusControllerConfiguredTokenTests.Stubs.class)
@TestPropertySource(properties = {
        "messaging.backend-token=abc"
})
class MessagingStatusControllerConfiguredTokenTests {

    @TestConfiguration
    static class Stubs {
        @Bean BackendTokenManager backendTokenManager() { return mock(BackendTokenManager.class); }
        @Bean KeycloakClient keycloakClient() { return mock(KeycloakClient.class); }
    }

    @Autowired MockMvc mockMvc;
    @Autowired BackendTokenManager backendTokenManager;

    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserDetailsService userDetailsService;

    @Test
    void configured_token_present_and_short_preview() throws Exception {
        when(backendTokenManager.getToken()).thenReturn(null);
        mockMvc.perform(get("/api/messaging/backend-token-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configuredBackendTokenPresent").value(true))
                .andExpect(jsonPath("$.configuredBackendTokenPreview").value("abc"))
                .andExpect(jsonPath("$.backendTokenCached").value(false));
    }
}
