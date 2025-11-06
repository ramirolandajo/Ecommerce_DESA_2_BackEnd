package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseServiceImplExtraTests {
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AuthService authService;
    @Mock
    private ProductService productService;
    @Mock
    private ECommerceEventService ecommerceEventService;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @Test
    void testSave_whenStatusNotPending_noEvent() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.CONFIRMED);
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        Purchase result = purchaseService.save(purchase);
        assertEquals(purchase, result);
        verify(ecommerceEventService, never()).emitRawEvent(anyString(), anyString());
    }

    @Test
    void testSave_whenObjectMapperThrows_noEvent() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        when(objectMapper.writeValueAsString(purchase)).thenThrow(new RuntimeException("JSON error"));
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        Purchase result = purchaseService.save(purchase);
        assertEquals(purchase, result);
        assertNotNull(result.getReservationTime());
        verify(ecommerceEventService, never()).emitRawEvent(anyString(), anyString());
    }

    @Test
    void testDeleteById_whenPurchaseNotFound() {
        when(purchaseRepository.findById(1)).thenReturn(Optional.empty());
        purchaseService.deleteById(1);
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void testDeleteById_whenAlreadyCancelled() {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.CANCELLED);
        when(purchaseRepository.findById(1)).thenReturn(Optional.of(purchase));
        purchaseService.deleteById(1);
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void testConfirmPurchase_whenNotFound() {
        when(purchaseRepository.findById(1)).thenReturn(Optional.empty());
        Purchase result = purchaseService.confirmPurchase(1);
        assertNull(result);
    }

    @Test
    void testConfirmPurchase_success() {
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        when(purchaseRepository.findById(1)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        Purchase result = purchaseService.confirmPurchase(1);
        assertEquals(Purchase.Status.CONFIRMED, result.getStatus());
        assertNotNull(result.getDate());
    }

    @Test
    void testGetPurchasesByUserEmail_whenUserNotFound() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(null);
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("test@email.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPurchasesByUserEmail_whenPurchasesNull() {
        User user = new User();
        user.setId(1);
        when(userRepository.findByEmail("test@email.com")).thenReturn(user);
        when(purchaseRepository.findByUser_Id(1)).thenReturn(null);
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("test@email.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPurchasesByUserEmail_whenPurchaseNotConfirmed() {
        User user = new User();
        user.setId(1);
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.PENDING);
        when(userRepository.findByEmail("test@email.com")).thenReturn(user);
        when(purchaseRepository.findByUser_Id(1)).thenReturn(List.of(purchase));
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("test@email.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPurchasesByUserEmail_whenCartNull() {
        User user = new User();
        user.setId(1);
        Purchase purchase = new Purchase();
        purchase.setStatus(Purchase.Status.CONFIRMED);
        purchase.setCart(null);
        when(userRepository.findByEmail("test@email.com")).thenReturn(user);
        when(purchaseRepository.findByUser_Id(1)).thenReturn(List.of(purchase));
        List<PurchaseInvoiceDTO> result = purchaseService.getPurchasesByUserEmail("test@email.com");
        assertEquals(1, result.size());
        assertEquals(0.0F, result.get(0).getTotalAmount());
        assertTrue(result.get(0).getProducts().isEmpty());
    }

    @Test
    void testGetEmailFromToken() {
        when(authService.getEmailFromToken("token")).thenReturn("email@test.com");
        String result = purchaseService.getEmailFromToken("token");
        assertEquals("email@test.com", result);
    }
}
