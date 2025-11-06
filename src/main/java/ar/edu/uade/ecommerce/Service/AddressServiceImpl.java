package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AddressServiceImpl implements ar.edu.uade.ecommerce.Service.AddressService {
    @Autowired
    private AddressRepository addressRepository;

    @Override
    public Address save(Address address) {
        return addressRepository.save(address);
    }

    @Override
    public Address findById(Integer id) {
        return addressRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        addressRepository.deleteById(id);
    }

    @Override
    public Address addAddress(Address address) {
        return addressRepository.save(address);
    }

    @Override
    public List<Address> getAddressesByUser(User user) {
        return addressRepository.findByUser(user);
    }



    @Override
    public void deleteAddress(Integer id, User user) {
        Optional<Address> opt = addressRepository.findById(id);
        if (opt.isEmpty()) {
            return;
        }
        Address address = opt.get();
        // Evitar NPE si user es null
        if (user != null && address.getUser() != null && Objects.equals(address.getUser().getId(), user.getId())) {
            addressRepository.deleteById(id);
        }
    }
}
