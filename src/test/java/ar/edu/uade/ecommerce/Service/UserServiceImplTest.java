package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.AddressRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testAddAddress() {
        Address address = new Address();
        when(addressRepository.save(address)).thenReturn(address);
        Address result = userService.addAddress(address);
        assertEquals(address, result);
        verify(addressRepository).save(address);
    }

    @Test
    void testUpdateAddress_success() {
        User user = new User();
        user.setId(1);
        Address existing = new Address();
        existing.setId(10);
        existing.setUser(user);
        existing.setDescription("old");
        Address update = new Address();
        update.setUser(user);
        update.setDescription("new");
        when(addressRepository.findById(10)).thenReturn(Optional.of(existing));
        when(addressRepository.save(existing)).thenReturn(existing);
        Address result = userService.updateAddress(10, update);
        assertEquals("new", result.getDescription());
        verify(addressRepository).findById(10);
        verify(addressRepository).save(existing);
    }

    @Test
    void testUpdateAddress_notFound() {
        Address update = new Address();
        when(addressRepository.findById(99)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateAddress(99, update));
        assertEquals("Dirección no encontrada", ex.getMessage());
        verify(addressRepository).findById(99);
    }

    @Test
    void testUpdateAddress_noPermission() {
        User user1 = new User();
        user1.setId(1);
        User user2 = new User();
        user2.setId(2);
        Address existing = new Address();
        existing.setId(10);
        existing.setUser(user1);
        Address update = new Address();
        update.setUser(user2);
        when(addressRepository.findById(10)).thenReturn(Optional.of(existing));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateAddress(10, update));
        assertEquals("No tienes permiso para modificar esta dirección", ex.getMessage());
        verify(addressRepository).findById(10);
    }

    @Test
    void testDeleteAddress_success() {
        User user = new User();
        user.setId(1);
        Address address = new Address();
        address.setId(10);
        address.setUser(user);
        when(addressRepository.findById(10)).thenReturn(Optional.of(address));
        userService.deleteAddress(10, user);
        verify(addressRepository).findById(10);
        verify(addressRepository).delete(address);
    }

    @Test
    void testDeleteAddress_notFound() {
        User user = new User();
        when(addressRepository.findById(99)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.deleteAddress(99, user));
        assertEquals("Dirección no encontrada", ex.getMessage());
        verify(addressRepository).findById(99);
    }

    @Test
    void testDeleteAddress_noPermission() {
        User user1 = new User();
        user1.setId(1);
        User user2 = new User();
        user2.setId(2);
        Address address = new Address();
        address.setId(10);
        address.setUser(user2);
        when(addressRepository.findById(10)).thenReturn(Optional.of(address));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.deleteAddress(10, user1));
        assertEquals("No tienes permiso para eliminar esta dirección", ex.getMessage());
        verify(addressRepository).findById(10);
    }

    @Test
    void testSaveUser_withPassword() {
        User user = new User();
        user.setPassword("1234");
        when(passwordEncoder.encode("1234")).thenReturn("hashed");
        when(userRepository.save(user)).thenReturn(user);
        User result = userService.saveUser(user);
        assertEquals("hashed", result.getPassword());
        verify(passwordEncoder).encode("1234");
        verify(userRepository).save(user);
    }

    @Test
    void testSaveUser_noPassword() {
        User user = new User();
        user.setPassword(null);
        when(userRepository.save(user)).thenReturn(user);
        User result = userService.saveUser(user);
        assertNull(result.getPassword());
        verify(userRepository).save(user);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void testSaveUser_emptyPassword() {
        User user = new User();
        user.setPassword("");
        when(userRepository.save(user)).thenReturn(user);
        User result = userService.saveUser(user);
        assertEquals("", result.getPassword());
        verify(userRepository).save(user);
        verify(passwordEncoder, never()).encode(any());
    }
}
