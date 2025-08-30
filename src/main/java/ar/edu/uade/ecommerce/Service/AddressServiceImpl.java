package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Address addAddress(Address address) {
        return addressRepository.save(address);
    }

    public List<Address> getAddressesByUser(ar.edu.uade.ecommerce.Entity.User user) {
        return user.getAddresses();
    }

    public void deleteAddress(Integer id, ar.edu.uade.ecommerce.Entity.User user) {
        Address address = addressRepository.findById(id).orElse(null);
        if (address != null && address.getUser() != null && address.getUser().getId().equals(user.getId())) {
            addressRepository.deleteById(id);
        }
    }
}
