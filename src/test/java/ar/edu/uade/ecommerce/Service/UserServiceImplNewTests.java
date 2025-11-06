package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.AddressRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
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
class UserServiceImplNewTests {
    @Mock AddressRepository addressRepository;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserServiceImpl service;

    @Test
    void addAddress_delegatesToRepository() {
        Address a = new Address();
        when(addressRepository.save(a)).thenReturn(a);
        assertSame(a, service.addAddress(a));
    }

    @Test
    void updateAddress_whenNotFound_throws() {
        when(addressRepository.findById(10)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateAddress(10, new Address()));
    }

    @Test
    void updateAddress_whenUserMismatch_throws() {
        User u1 = new User(); u1.setId(1);
        Address existing = new Address(); existing.setUser(u1);
        when(addressRepository.findById(10)).thenReturn(Optional.of(existing));
        User u2 = new User(); u2.setId(2);
        Address upd = new Address(); upd.setUser(u2);
        assertThrows(RuntimeException.class, () -> service.updateAddress(10, upd));
    }

    @Test
    void saveUser_hashesPasswordWhenPresent() {
        User u = new User(); u.setPassword("plain");
        when(passwordEncoder.encode("plain")).thenReturn("hashed");
        when(userRepository.save(u)).thenReturn(u);
        User out = service.saveUser(u);
        assertEquals("hashed", out.getPassword());
    }
}

