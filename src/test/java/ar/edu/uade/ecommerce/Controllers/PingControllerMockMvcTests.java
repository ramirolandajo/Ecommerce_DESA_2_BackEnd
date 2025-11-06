package ar.edu.uade.ecommerce.Controllers;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PingControllerMockMvcTests.Stubs.class)
class PingControllerMockMvcTests {

    @TestConfiguration
    static class Stubs {
        @Bean
        UserDetailsService userDetailsService() {
            UserDetails u = User.withUsername("u").password("p").roles("USER").build();
            return new InMemoryUserDetailsManager(u);
        }
    }

    @Autowired MockMvc mockMvc;
}
