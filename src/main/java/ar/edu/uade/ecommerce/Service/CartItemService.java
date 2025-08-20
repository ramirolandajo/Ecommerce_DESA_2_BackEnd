package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.CartItem;
import java.util.List;

public interface CartItemService {
    CartItem addCartItem(CartItem cartItem);
    CartItem updateCartItem(Integer id, CartItem cartItem);
    void removeCartItem(Integer id);
    List<CartItem> getCartItemsByCartId(Integer cartId);
}
