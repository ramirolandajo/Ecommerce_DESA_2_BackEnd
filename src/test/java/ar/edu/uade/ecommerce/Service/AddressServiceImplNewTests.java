package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplNewTests {
    @Mock
    AddressRepository addressRepository;

    @InjectMocks
    AddressServiceImpl service;

    @Test
    void save_delegatesToRepository() {
        Address a = new Address();
        when(addressRepository.save(a)).thenReturn(a);
        Address out = service.save(a);
        assertSame(a, out);
        verify(addressRepository).save(a);
    }

    @Test
    void findById_returnsNullWhenNotFound() {
        when(addressRepository.findById(9)).thenReturn(Optional.empty());
        assertNull(service.findById(9));
    }

    @Test
    void deleteAddress_whenAddressNotFound_shouldNotDelete() {
        when(addressRepository.findById(10)).thenReturn(Optional.empty());
        service.deleteAddress(10, new User());
        verify(addressRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteAddress_whenBelongsToUser_deletes() {
        User u = new User(); u.setId(1);
        Address a = new Address(); a.setId(10); a.setUser(u);
        when(addressRepository.findById(10)).thenReturn(Optional.of(a));
        service.deleteAddress(10, u);
        verify(addressRepository).deleteById(10);
    }

    @Test
    void deleteAddress_whenNotBelongsToUser_doesNothing() {
        User owner = new User(); owner.setId(1);
        User other = new User(); other.setId(2);
        Address a = new Address(); a.setId(10); a.setUser(owner);
        when(addressRepository.findById(10)).thenReturn(Optional.of(a));
        service.deleteAddress(10, other);
        verify(addressRepository, never()).deleteById(anyInt());
    }
}

