package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.CartRepository;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.Security.JwtUtil;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplNewTests {
    @Mock CartRepository cartRepository;
    @Mock UserRepository userRepository;
    @Mock JwtUtil jwtUtil;
    @Mock PurchaseRepository purchaseRepository;
    @Mock ar.edu.uade.ecommerce.Repository.ProductRepository productRepository;
    @Mock PurchaseService purchaseService;
    @Mock ECommerceEventService ecommerceEventService;

    @InjectMocks CartServiceImpl service;

    @Test
    void createCart_calculatesFinalPrice_andEmitsPendingEvent() {
        Product p = new Product(); p.setId(1); p.setPrice(10f);
        CartItem i = new CartItem(); i.setQuantity(2); i.setProduct(p);
        Product persisted = new Product(); persisted.setId(1); persisted.setPrice(10f); persisted.setStock(5);
        Cart cart = new Cart(); cart.setItems(java.util.List.of(i));
        when(productRepository.findById(1)).thenReturn(Optional.of(persisted));
        when(cartRepository.save(cart)).thenReturn(cart);

        Cart out = service.createCart(cart);
        assertEquals(20f, out.getFinalPrice());
        verify(ecommerceEventService).emitRawEvent(eq("POST: Compra pendiente"), anyString());
    }

    @Test
    void sendKafkaEvent_buildsJsonWithProductCode_whenCartPayload() {
        Product prod = new Product(); prod.setProductCode(555); prod.setTitle("A"); prod.setPrice(9.9f);
        CartItem item = new CartItem(); item.setProduct(prod); item.setQuantity(1);
        Cart cart = new Cart(); cart.setId(70); cart.setItems(List.of(item));
        when(purchaseRepository.findByCartId(70)).thenReturn(null);
        service.sendKafkaEvent("EV", cart);
        verify(ecommerceEventService).emitRawEvent(eq("EV"), anyString());
    }

    @Test
    void getEmailFromToken_delegatesToJwtUtil() {
        when(jwtUtil.extractUsername("t")).thenReturn("e@e.com");
        assertEquals("e@e.com", service.getEmailFromToken("t"));
    }

    @Test
    void isUserSessionActive_checksUserFlag() {
        User u = new User(); u.setSessionActive(true); when(userRepository.findByEmail("a@b")) .thenReturn(u);
        assertTrue(service.isUserSessionActive("a@b"));
    }
}

