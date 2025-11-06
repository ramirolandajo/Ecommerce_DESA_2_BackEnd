package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Repository.*;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import ar.edu.uade.ecommerce.Security.JwtUtil;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProductControllerNotImplementedMockMvcTests.Stubs.class)
class ProductControllerNotImplementedMockMvcTests {

    @TestConfiguration
    static class Stubs {
        @Bean ProductRepository productRepository() { return mock(ProductRepository.class); }
        @Bean ReviewRepository reviewRepository() { return mock(ReviewRepository.class); }
        @Bean CartItemRepository cartItemRepository() { return mock(CartItemRepository.class); }
        @Bean BrandRepository brandRepository() { return mock(BrandRepository.class); }
        @Bean CategoryRepository categoryRepository() { return mock(CategoryRepository.class); }
        @Bean FavouriteProductsRepository favouriteProductsRepository() { return mock(FavouriteProductsRepository.class); }
        @Bean UserRepository userRepository() { return mock(UserRepository.class); }
        @Bean ar.edu.uade.ecommerce.Service.AuthService authService() { return mock(ar.edu.uade.ecommerce.Service.AuthService.class); }
        @Bean ECommerceEventService ecommerceEventService() { return mock(ECommerceEventService.class); }
        @Bean EntityManager entityManager() { return mock(EntityManager.class); }
        @Bean EntityManagerFactory entityManagerFactory() { return mock(EntityManagerFactory.class); }
        @Bean JwtUtil jwtUtil() { return mock(JwtUtil.class); }
        @Bean UserDetailsService userDetailsService() {
            UserDetails u = User.withUsername("u").password("p").roles("USER").build();
            return new InMemoryUserDetailsManager(u);
        }
    }

    @Autowired MockMvc mockMvc;

    @Test void sync_not_implemented() throws Exception { mockMvc.perform(get("/products/sync")).andExpect(status().isNotImplemented()); }
    @Test void post_not_implemented() throws Exception { mockMvc.perform(post("/products")).andExpect(status().isNotImplemented()); }
    @Test void patch_simple_not_implemented() throws Exception { mockMvc.perform(patch("/products/simple")).andExpect(status().isNotImplemented()); }
    @Test void patch_edit_not_implemented() throws Exception { mockMvc.perform(patch("/products")).andExpect(status().isNotImplemented()); }
    @Test void patch_activate_not_implemented() throws Exception { mockMvc.perform(patch("/products/activate")).andExpect(status().isNotImplemented()); }
    @Test void patch_deactivate_not_implemented() throws Exception { mockMvc.perform(patch("/products/deactivate")).andExpect(status().isNotImplemented()); }
}
