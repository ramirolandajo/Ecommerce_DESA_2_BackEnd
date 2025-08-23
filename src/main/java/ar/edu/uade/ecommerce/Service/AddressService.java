package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;

public interface AddressService {
    Address save(Address address);
    Address findById(Integer id);
    void delete(Integer id);
}
