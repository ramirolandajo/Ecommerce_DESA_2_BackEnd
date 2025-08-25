package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Cart;

public interface CartService {
    Cart save(Cart cart);
    Cart findById(Integer id);
    void delete(Integer id);
}
