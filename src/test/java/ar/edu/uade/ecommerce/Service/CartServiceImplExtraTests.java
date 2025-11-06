package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.CartRepository;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplExtraTests {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ECommerceEventService ecommerceEventService;
    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void testSave() {
        Cart cart = new Cart();
        when(cartRepository.save(cart)).thenReturn(cart);
        Cart result = cartService.save(cart);
        assertEquals(cart, result);
    }

    @Test
    void testFindById_found() {
        Cart cart = new Cart();
        when(cartRepository.findById(1)).thenReturn(Optional.of(cart));
        Cart result = cartService.findById(1);
        assertEquals(cart, result);
    }

    @Test
    void testFindById_notFound() {
        when(cartRepository.findById(1)).thenReturn(Optional.empty());
        Cart result = cartService.findById(1);
        assertNull(result);
    }

    @Test
    void testFindAll() {
        List<Cart> carts = List.of(new Cart());
        when(cartRepository.findAll()).thenReturn(carts);
        List<Cart> result = cartService.findAll();
        assertEquals(carts, result);
    }

    @Test
    void testCreateCart_productNotFound() {
        Product product = new Product();
        product.setId(1);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    @Test
    void testCreateCart_insufficientStock() {
        Product product = new Product();
        product.setId(1);
        product.setStock(1);
        product.setPrice(10f);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
        assertTrue(ex.getMessage().contains("No hay suficiente stock"));
    }

    @Test
    void testCreateCart_nullProduct() {
        CartItem item = new CartItem();
        item.setProduct(null);
        item.setQuantity(1);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
        assertTrue(ex.getMessage().contains("Producto inválido"));
    }

    @Test
    void testUpdateCart_notFound() {
        Cart cart = new Cart();
        when(cartRepository.findById(1)).thenReturn(Optional.empty());
        Cart result = cartService.updateCart(1, cart);
        assertNull(result);
    }

    @Test
    void testUpdateCart_found() {
        Cart existing = new Cart();
        existing.setId(1);
        Cart update = new Cart();
        update.setFinalPrice(100f);
        User user = new User();
        update.setUser(user);
        when(cartRepository.findById(1)).thenReturn(Optional.of(existing));
        when(cartRepository.save(existing)).thenReturn(existing);
        Cart result = cartService.updateCart(1, update);
        assertEquals(existing, result);
        assertEquals(100f, existing.getFinalPrice());
        assertEquals(user, existing.getUser());
    }
}
