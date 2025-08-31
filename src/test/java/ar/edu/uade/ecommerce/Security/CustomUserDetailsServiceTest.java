package ar.edu.uade.ecommerce.Security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.UserRepository;

public class CustomUserDetailsServiceTest {
    @Test
    void testLoadUserByUsernameThrows() throws Exception {
        UserRepository mockRepo = mock(UserRepository.class);
        when(mockRepo.findByEmail("noexiste@email.com")).thenReturn(null);
        CustomUserDetailsService service = new CustomUserDetailsService();
        java.lang.reflect.Field repoField = CustomUserDetailsService.class.getDeclaredField("userRepository");
        repoField.setAccessible(true);
        repoField.set(service, mockRepo);
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("noexiste@email.com"));
    }

    @Test
    void testLoadUserByUsernameReturnsUserDetails() throws Exception {
        UserRepository mockRepo = mock(UserRepository.class);
        User user = new User();
        user.setEmail("test@email.com");
        user.setPassword("1234");
        user.setRole("USER");
        when(mockRepo.findByEmail("test@email.com")).thenReturn(user);

        CustomUserDetailsService service = new CustomUserDetailsService();
        java.lang.reflect.Field repoField = CustomUserDetailsService.class.getDeclaredField("userRepository");
        repoField.setAccessible(true);
        repoField.set(service, mockRepo);

        UserDetails details = service.loadUserByUsername("test@email.com");
        assertEquals("test@email.com", details.getUsername());
        assertEquals("1234", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
