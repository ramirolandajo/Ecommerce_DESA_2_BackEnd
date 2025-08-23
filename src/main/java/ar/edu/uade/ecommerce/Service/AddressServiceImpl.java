package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public Address save(Address address) {
        return addressRepository.save(address);
    }

    public Address findById(Integer id) {
        return addressRepository.findById(id).orElse(null);
    }

    public void delete(Integer id) {
        addressRepository.deleteById(id);
    }
}
