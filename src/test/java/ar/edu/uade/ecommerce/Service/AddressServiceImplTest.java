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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceImplTest {
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void testSave() {
        Address address = new Address();
        when(addressRepository.save(address)).thenReturn(address);
        Address result = addressService.save(address);
        assertEquals(address, result);
        verify(addressRepository).save(address);
    }

    @Test
    void testFindById_found() {
        Address address = new Address();
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));
        Address result = addressService.findById(1);
        assertEquals(address, result);
        verify(addressRepository).findById(1);
    }

    @Test
    void testFindById_notFound() {
        when(addressRepository.findById(2)).thenReturn(Optional.empty());
        Address result = addressService.findById(2);
        assertNull(result);
        verify(addressRepository).findById(2);
    }

    @Test
    void testDelete() {
        addressService.delete(1);
        verify(addressRepository).deleteById(1);
    }

    @Test
    void testAddAddress() {
        Address address = new Address();
        when(addressRepository.save(address)).thenReturn(address);
        Address result = addressService.addAddress(address);
        assertEquals(address, result);
        verify(addressRepository).save(address);
    }

    @Test
    void testGetAddressesByUser() {
        User user = new User();
        Address address1 = new Address();
        Address address2 = new Address();
        user.setAddresses(Arrays.asList(address1, address2));
        when(addressRepository.findByUser(user)).thenReturn(Arrays.asList(address1, address2));
        List<Address> result = addressService.getAddressesByUser(user);
        assertEquals(2, result.size());
        assertTrue(result.contains(address1));
        assertTrue(result.contains(address2));
    }

    @Test
    void testGetAddressesByUser_empty() {
        User user = new User();
        user.setAddresses(Collections.emptyList());
        List<Address> result = addressService.getAddressesByUser(user);
        assertTrue(result.isEmpty());
    }

   @Test
    void testDeleteAddress_addressExistsAndBelongsToUser() {
        User user = new User();
        user.setId(1);
        Address address = new Address();
        address.setId(10);
        address.setUser(user);
        lenient().when(addressRepository.findById(10)).thenReturn(Optional.of(address));
        addressService.deleteAddress(10, user);
        verify(addressRepository).deleteById(10);
    }

    @Test
    void testDeleteAddress_addressExistsButNotBelongsToUser() {
        User user = new User();
        user.setId(1);
        User otherUser = new User();
        otherUser.setId(2);
        Address address = new Address();
        address.setId(10);
        address.setUser(otherUser);
        when(addressRepository.findById(10)).thenReturn(Optional.of(address));
        addressService.deleteAddress(10, user);
        verify(addressRepository, never()).deleteById(anyInt());
    }

    @Test
    void testDeleteAddress_addressNotExists() {
        User user = new User();
        user.setId(1);
        when(addressRepository.findById(10)).thenReturn(Optional.empty());
        addressService.deleteAddress(10, user);
        verify(addressRepository, never()).deleteById(anyInt());
    }

    @Test
    void testDeleteAddress_addressUserIsNull() {
        User user = new User();
        user.setId(1);
        Address address = new Address();
        address.setId(10);
        address.setUser(null);
        when(addressRepository.findById(10)).thenReturn(Optional.of(address));
        addressService.deleteAddress(10, user);
        verify(addressRepository, never()).deleteById(anyInt());
    }
}
