package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Repository.AddressRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Address addAddress(Address address) {
        // El usuario ya está seteado en el controller
        return addressRepository.save(address);
    }

    @Override
    public Address updateAddress(Integer id, Address address) {
        Optional<Address> existingOpt = addressRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("Dirección no encontrada");
        }
        Address existing = existingOpt.get();
        // Validar que la dirección pertenezca al usuario
        if (!existing.getUser().getId().equals(address.getUser().getId())) {
            throw new RuntimeException("No tienes permiso para modificar esta dirección");
        }
        existing.setDescription(address.getDescription());
        return addressRepository.save(existing);
    }

    @Override
    public void deleteAddress(Integer id, User user) {
        Optional<Address> addressOpt = addressRepository.findById(id);
        if (addressOpt.isEmpty()) {
            throw new RuntimeException("Dirección no encontrada");
        }
        Address address = addressOpt.get();
        // Validar que la dirección pertenezca al usuario
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar esta dirección");
        }
        addressRepository.delete(address);
    }

    @Override
    public User saveUser(User user) {
        // Si la contraseña fue modificada, la hasheamos
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }
}
