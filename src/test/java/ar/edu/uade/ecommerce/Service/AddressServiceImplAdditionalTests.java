package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplAdditionalTests {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private Address address;
    private User owner;

    @BeforeEach
    void setUp() {
        address = new Address();
        address.setId(10);
        owner = new User();
        owner.setId(1);
        address.setUser(owner);
    }

    @Test
    void deleteAddress_whenUserIsNull_shouldNotDelete() {
        when(addressRepository.findById(10)).thenReturn(Optional.of(address));

        addressService.deleteAddress(10, null);

        verify(addressRepository, never()).deleteById(any());
    }

    @Test
    void deleteAddress_whenAddressDoesNotExist_shouldNotDelete() {
        when(addressRepository.findById(10)).thenReturn(Optional.empty());
        User u = new User(); u.setId(1);
        addressService.deleteAddress(10, u);
        verify(addressRepository, never()).deleteById(any());
    }

    @Test
    void deleteAddress_whenAddressBelongsToAnotherUser_shouldNotDelete() {
        when(addressRepository.findById(10)).thenReturn(Optional.of(address));
        User other = new User(); other.setId(2);
        addressService.deleteAddress(10, other);
        verify(addressRepository, never()).deleteById(any());
    }

    @Test
    void deleteAddress_whenAddressBelongsToUser_shouldDelete() {
        when(addressRepository.findById(10)).thenReturn(Optional.of(address));
        User u = new User(); u.setId(1);
        addressService.deleteAddress(10, u);
        verify(addressRepository).deleteById(10);
    }
}
