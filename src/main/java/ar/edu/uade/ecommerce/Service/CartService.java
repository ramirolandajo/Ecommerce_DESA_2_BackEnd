package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Cart;
import java.util.List;

public interface CartService {
    Cart save(Cart cart);
    Cart findById(Integer id);
    void delete(Integer id);
    List<Cart> findAll();
    // Métodos para crear, editar y eliminar carritos
    Cart createCart(Cart cart);
    Cart updateCart(Integer id, Cart cart);
    void deleteCart(Integer id);

    String getEmailFromToken(String token);

    boolean isUserSessionActive(String email);
}
