package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import ar.edu.uade.ecommerce.Security.JwtAuthenticationFilter;

@WebMvcTest(ProductSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProductSearchControllerMockMvcTests.Stubs.class)
class ProductSearchControllerMockMvcTests {

    @TestConfiguration
    static class Stubs {
        @Bean ProductRepository productRepository() { return mock(ProductRepository.class); }
        @Bean UserDetailsService userDetailsService() {
            UserDetails u = User.withUsername("u").password("p").roles("USER").build();
            return new InMemoryUserDetailsManager(u);
        }
        // Bean simulado del filtro JWT para evitar dependencias reales (JwtUtil)
        @Bean JwtAuthenticationFilter jwtAuthenticationFilter() { return mock(JwtAuthenticationFilter.class); }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ProductRepository productRepository;
}
