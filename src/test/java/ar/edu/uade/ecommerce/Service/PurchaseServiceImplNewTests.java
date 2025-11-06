package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplNewTests {
    @Mock PurchaseRepository purchaseRepository;
    // Reemplazo mock por instancia real para evitar problemas con JDK 23
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock UserRepository userRepository;
    @Mock ProductRepository productRepository;
    @Mock AuthService authService;
    @Mock ProductService productService;
    @Mock ECommerceEventService ecommerceEventService;

    @InjectMocks PurchaseServiceImpl service;

    @BeforeEach
    void init() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
    }

    @Test
    void save_whenPending_setsReservationAndEmits() throws Exception {
        Purchase p = new Purchase(); p.setStatus(Purchase.Status.PENDING);
        when(purchaseRepository.save(p)).thenReturn(p);
        Purchase out = service.save(p);
        assertSame(p, out);
        assertNotNull(out.getReservationTime());
        verify(ecommerceEventService).emitRawEvent(eq("ReserveStock"), anyString());
        verify(purchaseRepository).save(p);
    }

    @Test
    void confirmPurchase_whenMissing_returnsNull() {
        when(purchaseRepository.findById(1)).thenReturn(Optional.empty());
        assertNull(service.confirmPurchase(1));
    }

    @Test
    void confirmPurchase_found_setsConfirmedAndDate() {
        Purchase p = new Purchase(); p.setStatus(Purchase.Status.PENDING);
        when(purchaseRepository.findById(1)).thenReturn(Optional.of(p));
        when(purchaseRepository.save(p)).thenReturn(p);
        Purchase out = service.confirmPurchase(1);
        assertEquals(Purchase.Status.CONFIRMED, out.getStatus());
        assertNotNull(out.getDate());
    }

    @Test
    void releaseExpiredReservations_queriesRepoAndCancels() throws Exception {
        Purchase expired = new Purchase();
        expired.setStatus(Purchase.Status.PENDING);
        Cart cart = new Cart(); cart.setId(99);
        User u = new User(); u.setId(5); u.setEmail("e@e.com");
        cart.setUser(u);
        expired.setReservationTime(LocalDateTime.now().minusHours(5));
        expired.setCart(cart);
        when(purchaseRepository.findExpiredWithCartAndUser(eq(Purchase.Status.PENDING), any())).thenReturn(List.of(expired));
        when(purchaseRepository.save(expired)).thenReturn(expired);

        service.releaseExpiredReservations();

        assertEquals(Purchase.Status.CANCELLED, expired.getStatus());
        verify(ecommerceEventService).emitRawEvent(eq("ReleaseStock"), anyString());
        verify(purchaseRepository).save(expired);
    }

    @Test
    void getPurchasesWithCartByUserId_onlyConfirmedAndMapsProductCode() {
        Purchase confirmed = new Purchase(); confirmed.setId(1); confirmed.setStatus(Purchase.Status.CONFIRMED);
        Cart cart = new Cart(); cart.setId(10); cart.setFinalPrice(100f);
        CartItem item = new CartItem(); item.setId(7); item.setQuantity(2);
        Product prod = new Product(); prod.setId(3); prod.setTitle("T"); prod.setDescription("D"); prod.setPrice(12.5f); prod.setProductCode(999);
        item.setProduct(prod); cart.setItems(java.util.List.of(item));
        confirmed.setCart(cart);
        when(purchaseRepository.findByUser_Id(5)).thenReturn(java.util.List.of(confirmed));

        var out = service.getPurchasesWithCartByUserId(5);
        assertEquals(1, out.size());
        var dto = out.get(0);
        assertEquals(10, dto.getCart().getId());
        assertEquals(999, dto.getCart().getItems().get(0).getProduct().getProductCode());
    }
}
