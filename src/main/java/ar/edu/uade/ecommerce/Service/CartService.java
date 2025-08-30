package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Cart;
import ar.edu.uade.ecommerce.Entity.Purchase;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Entity.Product;

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

    User findUserByEmail(String email);

    Purchase createPurchase(Purchase purchase);

    void updateProductStock(Product product);
    void sendKafkaEvent(String eventName, Object payload);
    void revertProductStock(Cart cart);
    void confirmProductStock(Cart cart);

    Product getProductById(Integer id);
}
