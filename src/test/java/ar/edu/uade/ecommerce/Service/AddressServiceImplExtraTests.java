package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceImplExtraTests {
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void testGetAddressesByUser_whenUserIsNull() {
        List<Address> result = addressService.getAddressesByUser(null);
        assertNotNull(result);
        verify(addressRepository).findByUser(null);
    }

    @Test
    void testSave_whenRepositoryThrowsException() {
        Address address = new Address();
        when(addressRepository.save(address)).thenThrow(new RuntimeException("DB error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> addressService.save(address));
        assertEquals("DB error", ex.getMessage());
    }

    @Test
    void testDeleteAddress_whenUserIdIsNull() {
        User user = new User();
        // user.setId(null); // default is null
        Address address = new Address();
        address.setId(10);
        address.setUser(user);
        when(addressRepository.findById(10)).thenReturn(java.util.Optional.of(address));
        addressService.deleteAddress(10, user);
        verify(addressRepository).deleteById(10);
    }

    @Test
    void testDeleteAddress_whenAddressUserIdIsNull() {
        User user = new User();
        user.setId(1);
        Address address = new Address();
        address.setId(10);
        address.setUser(null);
        when(addressRepository.findById(10)).thenReturn(java.util.Optional.of(address));
        addressService.deleteAddress(10, user);
        verify(addressRepository, never()).deleteById(anyInt());
    }

    @Test
    void testGetAddressesByUser_whenRepositoryReturnsNull() {
        User user = new User();
        when(addressRepository.findByUser(user)).thenReturn(null);
        List<Address> result = addressService.getAddressesByUser(user);
        assertNull(result);
    }
}
