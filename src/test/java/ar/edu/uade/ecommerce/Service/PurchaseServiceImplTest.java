package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO;
import ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO.ProductDetailDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseServiceImplTest {
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private KafkaMockService kafkaMockService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AuthService authService;
    @Mock
    private ProductService productService;
    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @Test
    void testSave_pendingPurchase() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(null);
        when(objectMapper.writeValueAsString(purchase)).thenReturn("json");
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        Purchase result = purchaseService.save(purchase);
        assertEquals(purchase, result);
        assertNotNull(result.getReservationTime());
        verify(kafkaMockService).sendEvent(any(Event.class));
        verify(kafkaMockService).mockListener(any(Event.class));
    }

    @Test
    void testSave_errorKafka() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(null);
        when(objectMapper.writeValueAsString(purchase)).thenThrow(new RuntimeException());
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        Purchase result = purchaseService.save(purchase);
        assertEquals(purchase, result);
        assertNotNull(result.getReservationTime());
    }

    @Test
    void testSave_notPendingOrReservationNotNull() {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.CONFIRMED);
        purchase.setReservationTime(LocalDateTime.now());
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        Purchase result = purchaseService.save(purchase);
        assertEquals(purchase, result);
        // No se debe llamar a Kafka
        verify(kafkaMockService, never()).sendEvent(any());
        verify(kafkaMockService, never()).mockListener(any());
    }

    @Test
    void testFindById_found() {
        Purchase purchase = new Purchase();
        when(purchaseRepository.findById(1)).thenReturn(Optional.of(purchase));
        Purchase result = purchaseService.findById(1);
        assertEquals(purchase, result);
    }

    @Test
    void testFindById_notFound() {
        when(purchaseRepository.findById(2)).thenReturn(Optional.empty());
        Purchase result = purchaseService.findById(2);
        assertNull(result);
    }

    @Test
    void testFindAll() {
        Purchase p1 = new Purchase();
        Purchase p2 = new Purchase();
        when(purchaseRepository.findAll()).thenReturn(Arrays.asList(p1, p2));
        List<Purchase> result = purchaseService.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testDeleteById_foundNotCancelled() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        when(purchaseRepository.findById(1)).thenReturn(Optional.of(purchase));
        when(objectMapper.writeValueAsString(purchase)).thenReturn("json");
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        purchaseService.deleteById(1);
        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
        verify(kafkaMockService).sendEvent(any(Event.class));
        verify(kafkaMockService).mockListener(any(Event.class));
    }

    @Test
    void testDeleteById_alreadyCancelled() {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.CANCELLED);
        when(purchaseRepository.findById(1)).thenReturn(Optional.of(purchase));
        purchaseService.deleteById(1);
        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void testDeleteById_notFound() {
        when(purchaseRepository.findById(2)).thenReturn(Optional.empty());
        purchaseService.deleteById(2);
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void testDeleteById_errorKafka() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        when(purchaseRepository.findById(1)).thenReturn(Optional.of(purchase));
        when(objectMapper.writeValueAsString(purchase)).thenThrow(new RuntimeException());
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        purchaseService.deleteById(1);
        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
    }

    @Test
    void testConfirmPurchase_found() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        when(purchaseRepository.findById(1)).thenReturn(Optional.of(purchase));
        when(objectMapper.writeValueAsString(purchase)).thenReturn("json");
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        Purchase result = purchaseService.confirmPurchase(1);
        assertEquals(Purchase.Status.CONFIRMED, result.getStatus());
        assertNotNull(result.getDate());
        verify(kafkaMockService).sendEvent(any(Event.class));
        verify(kafkaMockService).mockListener(any(Event.class));
    }

    @Test
    void testConfirmPurchase_notFound() {
        when(purchaseRepository.findById(2)).thenReturn(Optional.empty());
        Purchase result = purchaseService.confirmPurchase(2);
        assertNull(result);
    }

    @Test
    void testConfirmPurchase_errorKafka() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        when(purchaseRepository.findById(1)).thenReturn(Optional.of(purchase));
        when(objectMapper.writeValueAsString(purchase)).thenThrow(new RuntimeException());
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        Purchase result = purchaseService.confirmPurchase(1);
        assertEquals(Purchase.Status.CONFIRMED, result.getStatus());
    }

    @Test
    void testAddProductToCart() {
        purchaseService.addProductToCart(1, 2, 3);
        verify(kafkaMockService).sendEvent(any(Event.class));
        verify(kafkaMockService).mockListener(any(Event.class));
    }

    @Test
    void testEditCartItem() {
        purchaseService.editCartItem(1, 5);
        verify(kafkaMockService).sendEvent(any(Event.class));
        verify(kafkaMockService).mockListener(any(Event.class));
    }

    @Test
    void testRemoveProductFromCart() {
        purchaseService.removeProductFromCart(1);
        verify(kafkaMockService).sendEvent(any(Event.class));
        verify(kafkaMockService).mockListener(any(Event.class));
    }

    @Test
    void testReleaseExpiredReservations_expired() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        when(purchaseRepository.findAll()).thenReturn(List.of(purchase));
        when(objectMapper.writeValueAsString(purchase)).thenReturn("json");
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        purchaseService.releaseExpiredReservations();
        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
        verify(kafkaMockService).sendEvent(any(Event.class));
        verify(kafkaMockService).mockListener(any(Event.class));
    }

    @Test
    void testReleaseExpiredReservations_notExpired() {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(2));
        when(purchaseRepository.findAll()).thenReturn(List.of(purchase));
        purchaseService.releaseExpiredReservations();
        assertEquals(Purchase.Status.PENDING, purchase.getStatus());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void testReleaseExpiredReservations_errorKafka() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        when(purchaseRepository.findAll()).thenReturn(List.of(purchase));
        when(objectMapper.writeValueAsString(purchase)).thenThrow(new RuntimeException());
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        purchaseService.releaseExpiredReservations();
        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
    }

    @Test
    void testReleaseExpiredReservations_expiredWithException() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        when(purchaseRepository.findAll()).thenReturn(List.of(purchase));
        when(objectMapper.writeValueAsString(purchase)).thenThrow(new RuntimeException("Kafka error"));
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        purchaseService.releaseExpiredReservations();
        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
        // El catch del try debe ser cubierto
    }

    @Test
    void testReleaseExpiredReservations_reservationTimeNull() {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(null);
        when(purchaseRepository.findAll()).thenReturn(List.of(purchase));
        purchaseService.releaseExpiredReservations();
        // No debe cancelar ni guardar la compra
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void testGetPurchasesByUserEmail_userFound_confirmed() {
        User user = new User(); user.setId(1);
        Purchase purchase = new Purchase(); purchase.setId(10); purchase.setStatus(Purchase.Status.CONFIRMED);
        Cart cart = new Cart(); cart.setFinalPrice(100.0F); cart.setItems(new ArrayList<>());
        purchase.setCart(cart); purchase.setDate(LocalDateTime.now());
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(purchaseRepository.findByUser_Id(1)).thenReturn(List.of(purchase));
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("test@mail.com");
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getPurchaseId());
    }

    @Test
    void testGetPurchasesByUserEmail_userNotFound() {
        when(userRepository.findByEmail("notfound@mail.com")).thenReturn(null);
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("notfound@mail.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPurchasesByUserEmail_noConfirmedPurchases() {
        User user = new User(); user.setId(1);
        Purchase purchase = new Purchase(); purchase.setId(10); purchase.setStatus(Purchase.Status.PENDING);
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(purchaseRepository.findByUser_Id(1)).thenReturn(List.of(purchase));
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("test@mail.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPurchasesByUserEmail_userFound_confirmedWithCartItems() {
        User user = new User(); user.setId(1);
        Purchase purchase = new Purchase(); purchase.setId(10); purchase.setStatus(Purchase.Status.CONFIRMED);
        Cart cart = new Cart(); cart.setFinalPrice(100.0F);
        CartItem item = new CartItem(); item.setId(5);
        cart.setItems(List.of(item));
        purchase.setCart(cart); purchase.setDate(LocalDateTime.now());
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(purchaseRepository.findByUser_Id(1)).thenReturn(List.of(purchase));
        Product product = new Product();
        product.setId(5);
        product.setDescription("desc");
        product.setStock(10);
        product.setPrice(99.99F);
        when(productRepository.findById(5)).thenReturn(Optional.of(product));
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("test@mail.com");
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getPurchaseId());
        assertEquals(1, result.get(0).getProducts().size());
        assertEquals("desc", result.get(0).getProducts().get(0).getDescription());
    }

    @Test
    void testGetPurchasesByUserEmail_cartItemProductNotFound() {
        User user = new User(); user.setId(1);
        Purchase purchase = new Purchase(); purchase.setId(10); purchase.setStatus(Purchase.Status.CONFIRMED);
        Cart cart = new Cart(); cart.setFinalPrice(100.0F);
        CartItem item = new CartItem(); item.setId(99);
        cart.setItems(List.of(item));
        purchase.setCart(cart); purchase.setDate(LocalDateTime.now());
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(purchaseRepository.findByUser_Id(1)).thenReturn(List.of(purchase));
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("test@mail.com");
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getProducts().size());
    }

    @Test
    void testGetPurchasesByUserEmail_cartNull() {
        User user = new User(); user.setId(1);
        Purchase purchase = new Purchase(); purchase.setId(10); purchase.setStatus(Purchase.Status.CONFIRMED);
        purchase.setCart(null);
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(purchaseRepository.findByUser_Id(1)).thenReturn(List.of(purchase));
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("test@mail.com");
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getProducts().size());
    }

    @Test
    void testGetPurchasesByUserEmail_cartItemsNull() {
        User user = new User(); user.setId(1);
        Purchase purchase = new Purchase(); purchase.setId(10); purchase.setStatus(Purchase.Status.CONFIRMED);
        Cart cart = new Cart(); cart.setFinalPrice(100.0F);
        cart.setItems(null); // items es null
        purchase.setCart(cart); purchase.setDate(LocalDateTime.now());
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(purchaseRepository.findByUser_Id(1)).thenReturn(List.of(purchase));
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("test@mail.com");
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getProducts().size());
    }

    @Test
    void testFindLastPendingPurchaseByUserWithinHours_found() {
        Purchase purchase = new Purchase();
        purchase.setReservationTime(LocalDateTime.now().minusHours(1));
        when(purchaseRepository.findByUser_IdAndStatusOrderByReservationTimeDesc(1, Purchase.Status.PENDING)).thenReturn(List.of(purchase));
        Purchase result = purchaseService.findLastPendingPurchaseByUserWithinHours(1, 2);
        assertEquals(purchase, result);
    }

    @Test
    void testFindLastPendingPurchaseByUserWithinHours_notFound() {
        Purchase purchase = new Purchase();
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        when(purchaseRepository.findByUser_IdAndStatusOrderByReservationTimeDesc(1, Purchase.Status.PENDING)).thenReturn(List.of(purchase));
        Purchase result = purchaseService.findLastPendingPurchaseByUserWithinHours(1, 2);
        assertNull(result);
    }

    @Test
    void testFindLastPendingPurchaseByUserWithinHours_purchasesNull() {
        when(purchaseRepository.findByUser_IdAndStatusOrderByReservationTimeDesc(1, Purchase.Status.PENDING)).thenReturn(null);
        Purchase result = purchaseService.findLastPendingPurchaseByUserWithinHours(1, 2);
        assertNull(result);
    }

    @Test
    void testFindLastPendingPurchaseByUserWithinHours_emptyList() {
        when(purchaseRepository.findByUser_IdAndStatusOrderByReservationTimeDesc(1, Purchase.Status.PENDING)).thenReturn(Collections.emptyList());
        Purchase result = purchaseService.findLastPendingPurchaseByUserWithinHours(1, 2);
        assertNull(result);
    }

    @Test
    void testFindLastPendingPurchaseByUserWithinHours_allReservationTimesNull() {
        Purchase purchase1 = new Purchase();
        purchase1.setReservationTime(null);
        Purchase purchase2 = new Purchase();
        purchase2.setReservationTime(null);
        when(purchaseRepository.findByUser_IdAndStatusOrderByReservationTimeDesc(1, Purchase.Status.PENDING)).thenReturn(List.of(purchase1, purchase2));
        Purchase result = purchaseService.findLastPendingPurchaseByUserWithinHours(1, 2);
        assertNull(result);
    }

    @Test
    void testMockStockChange() {
        Product product = new Product();
        when(productService.updateProductStock(1, 10)).thenReturn(product);
        String result = purchaseService.mockStockChange(1, 10);
        assertTrue(result.contains("Producto actualizado"));
    }

    @Test
    void testGetEmailFromToken() {
        when(authService.getEmailFromToken("token")).thenReturn("test@mail.com");
        String email = purchaseService.getEmailFromToken("token");
        assertEquals("test@mail.com", email);
    }

    @Test
    void testFindByUserId() {
        Purchase purchase = new Purchase();
        when(purchaseRepository.findByUser_Id(1)).thenReturn(List.of(purchase));
        List<Purchase> result = purchaseService.findByUserId(1);
        assertEquals(1, result.size());
    }

    @Test
    void testSave_pendingWithReservationAlreadySet() {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now());
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        Purchase result = purchaseService.save(purchase);
        assertEquals(purchase, result);
        // No se debe llamar a Kafka
        verify(kafkaMockService, never()).sendEvent(any());
        verify(kafkaMockService, never()).mockListener(any());
    }
}
