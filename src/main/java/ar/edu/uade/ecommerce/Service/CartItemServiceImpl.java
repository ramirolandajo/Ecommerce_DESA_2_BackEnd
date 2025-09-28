package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.CartItem;
import ar.edu.uade.ecommerce.Repository.CartItemRepository;
import ar.edu.uade.ecommerce.Entity.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;

@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ECommerceEventService ecommerceEventService;

    @Override
    public CartItem addCartItem(CartItem cartItem) {
        CartItem saved = cartItemRepository.save(cartItem);
        try {
            String json = objectMapper.writeValueAsString(saved);
            Event ev = new Event("CartItemAdded", json);
            ecommerceEventService.emitRawEvent(ev.getType(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return saved;
    }

    @Override
    public CartItem updateCartItem(Integer id, CartItem cartItem) {
        Optional<CartItem> existing = cartItemRepository.findById(id);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setId(cartItem.getId());
            item.setQuantity(cartItem.getQuantity());
            CartItem updated = cartItemRepository.save(item);
            try {
                String json = objectMapper.writeValueAsString(updated);
                Event ev = new Event("CartItemUpdated", json);
                ecommerceEventService.emitRawEvent(ev.getType(), json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return updated;
        }
        return null;
    }

    @Override
    public void removeCartItem(Integer id) {
        Optional<CartItem> item = cartItemRepository.findById(id);
        item.ifPresent(i -> {
            try {
                String json = objectMapper.writeValueAsString(i);
                Event ev = new Event("CartItemRemoved", json);
                ecommerceEventService.emitRawEvent(ev.getType(), json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        cartItemRepository.deleteById(id);
    }

    @Override
    public List<CartItem> getCartItemsByCartId(Integer cartId) {
        // Suponiendo que CartItemRepository tiene un método para esto
        return cartItemRepository.findAll().stream()
                .filter(item -> item.getCart().getId().equals(cartId))
                .toList();
    }
}
