package ar.edu.uade.ecommerce.ventas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VentasHealthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(VentasHealthControllerEnvDefaultsTests.Stubs.class)
class VentasHealthControllerEnvDefaultsTests {

    @TestConfiguration
    static class Stubs {
        @Bean VentasConsumerMonitorService monitor() { return mock(VentasConsumerMonitorService.class); }
        @Bean Environment env() { return mock(Environment.class); }
        @Bean UserDetailsService userDetailsService() {
            UserDetails u = User.withUsername("u").password("p").roles("USER").build();
            return new InMemoryUserDetailsManager(u);
        }
    }

    @Autowired MockMvc mockMvc;

}

