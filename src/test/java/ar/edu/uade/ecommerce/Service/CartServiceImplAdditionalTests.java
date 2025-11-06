package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.CartRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplAdditionalTests {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ar.edu.uade.ecommerce.Repository.ProductRepository productRepository;
    @Mock
    private ar.edu.uade.ecommerce.messaging.ECommerceEventService ecommerceEventService;

    @InjectMocks
    private CartServiceImpl cartService;

    private Product product;
    private CartItem item;
    private Cart cart;

    @BeforeEach
    void setUp() {
        product = new Product(); product.setId(1); product.setStock(5); product.setPrice(3.0f); product.setProductCode(100);
        item = new CartItem(); item.setId(2); item.setProduct(product); item.setQuantity(2);
        cart = new Cart(); cart.setId(3); cart.setItems(new ArrayList<>());
        cart.getItems().add(item);
    }

    @Test
    void createCart_success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Cart created = cartService.createCart(cart);
        assertNotNull(created);
        assertEquals(6.0f, created.getFinalPrice());
        verify(cartRepository).save(created);
        // El método sendKafkaEvent usa ecommerceEventService.emitRawEvent; verificar llamada
        verify(ecommerceEventService, atLeastOnce()).emitRawEvent(anyString(), anyString());
    }

    @Test
    void createCart_productNotFound_throws() {
        when(productRepository.findById(1)).thenReturn(Optional.empty());
        Cart c = new Cart(); c.setItems(List.of(item));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cartService.createCart(c));
        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    @Test
    void isUserSessionActive_checksSession() {
        User u = new User(); u.setId(10); u.setSessionActive(true);
        when(userRepository.findByEmail("a@b.com")).thenReturn(u);
        assertTrue(cartService.isUserSessionActive("a@b.com"));
        when(userRepository.findByEmail("a@b.com")).thenReturn(null);
        assertFalse(cartService.isUserSessionActive("a@b.com"));
    }

    @Test
    void revertProductStock_updatesStockAndSendsEvent() {
        // preparar carrito con items y producto
        Product p = new Product(); p.setId(50); p.setStock(1); p.setProductCode(200); p.setPrice(4.0f);
        CartItem it = new CartItem(); it.setProduct(p); it.setQuantity(2);
        Cart c = new Cart(); c.setId(60); c.setItems(new ArrayList<>()); c.getItems().add(it);

        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.revertProductStock(c);

        // stock incrementado
        assertEquals(3, p.getStock());
        verify(productRepository).save(p);
        verify(ecommerceEventService).emitRawEvent(anyString(), anyString());
    }
}
