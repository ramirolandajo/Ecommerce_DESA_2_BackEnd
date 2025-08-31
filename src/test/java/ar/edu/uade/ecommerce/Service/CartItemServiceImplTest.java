package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.CartItem;
import ar.edu.uade.ecommerce.Entity.Cart;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Entity.Event;
import ar.edu.uade.ecommerce.Repository.CartItemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartItemServiceImplTest {
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private KafkaMockService kafkaMockService;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private CartItemServiceImpl cartItemService;

    @Test
    void testAddCartItem_success() throws Exception {
        CartItem cartItem = new CartItem();
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(objectMapper.writeValueAsString(cartItem)).thenReturn("json");
        CartItem result = cartItemService.addCartItem(cartItem);
        assertEquals(cartItem, result);
        verify(cartItemRepository).save(cartItem);
        verify(objectMapper).writeValueAsString(cartItem);
        verify(kafkaMockService).sendEvent(any(Event.class));
    }

    @Test
    void testAddCartItem_jsonException() throws Exception {
        CartItem cartItem = new CartItem();
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(objectMapper.writeValueAsString(cartItem)).thenThrow(JsonProcessingException.class);
        CartItem result = cartItemService.addCartItem(cartItem);
        assertEquals(cartItem, result);
        verify(cartItemRepository).save(cartItem);
        verify(objectMapper).writeValueAsString(cartItem);
        verify(kafkaMockService, never()).sendEvent(any());
    }

    @Test
    void testUpdateCartItem_success() throws Exception {
        CartItem existing = new CartItem();
        existing.setId(1);
        existing.setQuantity(2);
        CartItem update = new CartItem();
        update.setId(1);
        update.setQuantity(5);
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).thenReturn(existing);
        when(objectMapper.writeValueAsString(existing)).thenReturn("json");
        CartItem result = cartItemService.updateCartItem(1, update);
        assertEquals(existing, result);
        assertEquals(5, result.getQuantity());
        verify(cartItemRepository).findById(1);
        verify(cartItemRepository).save(existing);
        verify(objectMapper).writeValueAsString(existing);
        verify(kafkaMockService).sendEvent(any(Event.class));
    }

    @Test
    void testUpdateCartItem_notFound() throws Exception {
        CartItem update = new CartItem();
        when(cartItemRepository.findById(99)).thenReturn(Optional.empty());
        CartItem result = cartItemService.updateCartItem(99, update);
        assertNull(result);
        verify(cartItemRepository).findById(99);
        verify(cartItemRepository, never()).save(any());
        verify(objectMapper, never()).writeValueAsString(any());
        verify(kafkaMockService, never()).sendEvent(any());
    }

    @Test
    void testUpdateCartItem_jsonException() throws Exception {
        CartItem existing = new CartItem();
        existing.setId(1);
        existing.setQuantity(2);
        CartItem update = new CartItem();
        update.setId(1);
        update.setQuantity(5);
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).thenReturn(existing);
        when(objectMapper.writeValueAsString(existing)).thenThrow(JsonProcessingException.class);
        CartItem result = cartItemService.updateCartItem(1, update);
        assertEquals(existing, result);
        verify(cartItemRepository).findById(1);
        verify(cartItemRepository).save(existing);
        verify(objectMapper).writeValueAsString(existing);
        verify(kafkaMockService, never()).sendEvent(any());
    }

    @Test
    void testRemoveCartItem_success() throws Exception {
        CartItem item = new CartItem();
        item.setId(1);
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(item));
        when(objectMapper.writeValueAsString(item)).thenReturn("json");
        cartItemService.removeCartItem(1);
        verify(cartItemRepository).findById(1);
        verify(objectMapper).writeValueAsString(item);
        verify(kafkaMockService).sendEvent(any(Event.class));
        verify(cartItemRepository).deleteById(1);
    }

    @Test
    void testRemoveCartItem_notFound() throws Exception {
        when(cartItemRepository.findById(99)).thenReturn(Optional.empty());
        cartItemService.removeCartItem(99);
        verify(cartItemRepository).findById(99);
        verify(objectMapper, never()).writeValueAsString(any());
        verify(kafkaMockService, never()).sendEvent(any());
        verify(cartItemRepository).deleteById(99);
    }

    @Test
    void testRemoveCartItem_jsonException() throws Exception {
        CartItem item = new CartItem();
        item.setId(1);
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(item));
        when(objectMapper.writeValueAsString(item)).thenThrow(JsonProcessingException.class);
        cartItemService.removeCartItem(1);
        verify(cartItemRepository).findById(1);
        verify(objectMapper).writeValueAsString(item);
        verify(kafkaMockService, never()).sendEvent(any());
        verify(cartItemRepository).deleteById(1);
    }

    @Test
    void testGetCartItemsByCartId() {
        Cart cart = new Cart();
        cart.setId(5);
        CartItem item1 = new CartItem(); item1.setCart(cart);
        CartItem item2 = new CartItem(); item2.setCart(cart);
        Cart otherCart = new Cart(); otherCart.setId(6);
        CartItem item3 = new CartItem(); item3.setCart(otherCart);
        List<CartItem> all = Arrays.asList(item1, item2, item3);
        when(cartItemRepository.findAll()).thenReturn(all);
        List<CartItem> result = cartItemService.getCartItemsByCartId(5);
        assertEquals(2, result.size());
        assertTrue(result.contains(item1));
        assertTrue(result.contains(item2));
        assertFalse(result.contains(item3));
        verify(cartItemRepository).findAll();
    }
}
