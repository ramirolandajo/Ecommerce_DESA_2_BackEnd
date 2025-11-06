package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;

import ar.edu.uade.ecommerce.Security.JwtAuthenticationFilter;
import ar.edu.uade.ecommerce.Security.JwtUtil;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessagingStatusController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MessagingStatusControllerKeycloakPreviewShortTests.Stubs.class)
class MessagingStatusControllerKeycloakPreviewShortTests {

    @TestConfiguration
    static class Stubs {
        @Bean BackendTokenManager backendTokenManager() { return mock(BackendTokenManager.class); }
        @Bean KeycloakClient keycloakClient() { return mock(KeycloakClient.class); }
        @Bean JwtAuthenticationFilter jwtAuthenticationFilter() { return mock(JwtAuthenticationFilter.class); }
        @Bean JwtUtil jwtUtil() { return mock(JwtUtil.class); }
        @Bean UserDetailsService userDetailsService() {
            UserDetails u = User.withUsername("u").password("p").roles("USER").build();
            return new InMemoryUserDetailsManager(u);
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired KeycloakClient keycloakClient;

    @Test
    void keycloak_token_short_not_truncated() throws Exception {
        when(keycloakClient.getClientAccessTokenInfo()).thenReturn(new KeycloakClient.TokenInfo("short", 5));
        mockMvc.perform(get("/api/messaging/backend-token-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakClientAvailable").value(true))
                .andExpect(jsonPath("$.keycloakAccessTokenPreview").value("short"));
    }
}
