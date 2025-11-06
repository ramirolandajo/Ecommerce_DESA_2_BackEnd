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
class AddressServiceImplMoreTests {

    @Mock AddressRepository addressRepository;
    @InjectMocks AddressServiceImpl service;

    @Test
    void deleteAddress_deletesWhenOwnerMatches() {
        User u = new User(); u.setId(1);
        Address a = new Address(); a.setId(10); a.setUser(u);
        when(addressRepository.findById(10)).thenReturn(Optional.of(a));
        service.deleteAddress(10, u);
        verify(addressRepository).deleteById(10);
    }

    @Test
    void deleteAddress_doesNothingWhenOwnerDiffers() {
        User u1 = new User(); u1.setId(1);
        User u2 = new User(); u2.setId(2);
        Address a = new Address(); a.setId(10); a.setUser(u1);
        when(addressRepository.findById(10)).thenReturn(Optional.of(a));
        service.deleteAddress(10, u2);
        verify(addressRepository, never()).deleteById(anyInt());
    }
}

