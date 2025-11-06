package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.AddressRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplAdditionalTests {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    // address moved to local variables where needed to avoid unused-field warning

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setPassword("plain");
    }

    @Test
    void updateAddress_whenNotFound_throws() {
        when(addressRepository.findById(11)).thenReturn(Optional.empty());
        Address a = new Address(); a.setUser(user);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateAddress(11, a));
        assertTrue(ex.getMessage().contains("Dirección no encontrada"));
    }

    @Test
    void updateAddress_whenDifferentUser_throws() {
        User other = new User(); other.setId(2);
        Address existing = new Address(); existing.setId(11); existing.setUser(other);
        when(addressRepository.findById(11)).thenReturn(Optional.of(existing));
        Address a = new Address(); a.setUser(user);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateAddress(11, a));
        assertTrue(ex.getMessage().contains("No tienes permiso"));
    }

    @Test
    void saveUser_hashesPasswordWhenProvided() {
        when(passwordEncoder.encode("plain")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        User saved = userService.saveUser(user);
        assertEquals("hashed", saved.getPassword());
        verify(userRepository).save(saved);
    }

    @Test
    void deleteAddress_authorizationAndDelete() {
        User owner = new User(); owner.setId(1);
        Address existing = new Address(); existing.setId(20); existing.setUser(owner);
        when(addressRepository.findById(20)).thenReturn(Optional.of(existing));
        // Should not throw
        assertDoesNotThrow(() -> userService.deleteAddress(20, owner));
        verify(addressRepository).delete(existing);
    }
}
