package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Cart;
import ar.edu.uade.ecommerce.Repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;

    @Override
    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Cart findById(Integer id) {
        return cartRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        cartRepository.deleteById(id);
    }
}
