package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;

import java.util.List;

public interface AddressService {
    Address save(Address address);
    Address findById(Integer id);
    void delete(Integer id);
    Address addAddress(Address address);
    List<Address> getAddressesByUser(ar.edu.uade.ecommerce.Entity.User user);
    void deleteAddress(Integer id, ar.edu.uade.ecommerce.Entity.User user);
}
