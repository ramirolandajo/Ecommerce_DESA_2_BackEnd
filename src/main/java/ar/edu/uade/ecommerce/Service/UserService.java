package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;

public interface UserService {
    Address addAddress(Address address);
    Address updateAddress(Integer id, Address address);
    void deleteAddress(Integer id, User user);
    User saveUser(User user);
}
