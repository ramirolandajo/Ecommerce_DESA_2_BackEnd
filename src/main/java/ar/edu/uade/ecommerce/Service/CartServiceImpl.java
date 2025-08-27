package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Cart;
import ar.edu.uade.ecommerce.Repository.CartRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

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

    @Override
    public List<Cart> findAll() {
        return cartRepository.findAll();
    }

    @Override
    public Cart createCart(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateCart(Integer id, Cart cart) {
        Cart existing = cartRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setFinalPrice(cart.getFinalPrice());
            existing.setUser(cart.getUser());
            return cartRepository.save(existing);
        }
        return null;
    }

    @Override
    public void deleteCart(Integer id) {
        cartRepository.deleteById(id);
    }

    @Override
    public String getEmailFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }

    @Override
    public boolean isUserSessionActive(String email) {
        User user = userRepository.findByEmail(email);
        return user != null && Boolean.TRUE.equals(user.getSessionActive());
    }
}
