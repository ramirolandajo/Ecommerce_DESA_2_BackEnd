package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.CartItem;
import ar.edu.uade.ecommerce.Repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ar.edu.uade.ecommerce.Entity.Event;

@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ECommerceEventService ecommerceEventService;


    @Override
    public List<CartItem> getCartItemsByCartId(Integer cartId) {
        // Suponiendo que CartItemRepository tiene un método para esto
        return cartItemRepository.findAll().stream()
                .filter(item -> item.getCart().getId().equals(cartId))
                .toList();
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem findById(Integer id) {
        return cartItemRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        cartItemRepository.deleteById(id);
    }

    @Override
    public List<CartItem> findByCartId(Integer cartId) {
        return cartItemRepository.findByCartId(cartId);
    }
}
