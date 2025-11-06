package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.CartItem;
import java.util.List;

public interface CartItemService {
    CartItem save(CartItem cartItem);
    CartItem findById(Integer id);
    void delete(Integer id);
    List<CartItem> findByCartId(Integer cartId);

    List<CartItem> getCartItemsByCartId(Integer cartId);
}
