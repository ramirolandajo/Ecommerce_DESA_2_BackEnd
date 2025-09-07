package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO;
import ar.edu.uade.ecommerce.Entity.Purchase;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import ar.edu.uade.ecommerce.Service.PurchaseService;
import ar.edu.uade.ecommerce.Service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PurchaseControllerTest {
    @Mock
    private PurchaseService purchaseService;
    @Mock
    private AuthService authService;
    @Mock
    private CartService cartService;

    @InjectMocks
    private PurchaseController purchaseController;

    @Test
    void testGetAllPurchases() {
        when(purchaseService.findAll()).thenReturn(List.of(new Purchase()));
        List<Purchase> result = purchaseController.getAllPurchases();
        assertEquals(1, result.size());
    }

    @Test
    void testGetPurchaseById_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Purchase purchase = new Purchase();
        ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO dto = new ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO();
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.findById(1)).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO> response = purchaseController.getPurchaseById(token, 1);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetPurchaseById_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = purchaseController.getPurchaseById(token, 1);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPurchaseById_UserNull() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<?> response = purchaseController.getPurchaseById(token, 1);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPurchaseById_PurchaseNull() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.findById(1)).thenReturn(null);
        ResponseEntity<?> response = purchaseController.getPurchaseById(token, 1);
        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testCreatePurchase_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Purchase purchase = new Purchase();
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.save(any(Purchase.class))).thenReturn(purchase);
        ResponseEntity<Purchase> response = purchaseController.createPurchase(token, purchase);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreatePurchase_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        Purchase purchase = new Purchase();
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = purchaseController.createPurchase(token, purchase);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreatePurchase_UserNull() {
        String token = "Bearer testtoken";
        Purchase purchase = new Purchase();
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<?> response = purchaseController.createPurchase(token, purchase);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeletePurchase_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Purchase purchase = new Purchase();
        purchase.setCart(mock(ar.edu.uade.ecommerce.Entity.Cart.class));
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.findById(1)).thenReturn(purchase);
        doNothing().when(cartService).revertProductStock(any());
        doNothing().when(purchaseService).deleteById(1);
        ResponseEntity<?> response = purchaseController.deletePurchase(token, 1);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Compra eliminada correctamente"));
    }

    @Test
    void testDeletePurchase_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = purchaseController.deletePurchase(token, 1);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeletePurchase_TokenInvalido() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn(null);
        ResponseEntity<?> response = purchaseController.deletePurchase(token, 1);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeletePurchase_EmailNull() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn(null);
        when(authService.getUserByEmail(null)).thenReturn(null);
        ResponseEntity<?> response = purchaseController.deletePurchase(token, 1);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeletePurchase_UserNull() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<?> response = purchaseController.deletePurchase(token, 1);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeletePurchase_SessionInactive() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = purchaseController.deletePurchase(token, 1);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeletePurchase_PurchaseNull() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.findById(1)).thenReturn(null);
        ResponseEntity<?> response = purchaseController.deletePurchase(token, 1);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Compra eliminada correctamente"));
    }

    @Test
    void testDeletePurchase_PurchaseSinCart() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Purchase purchase = new Purchase();
        purchase.setCart(null);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.findById(1)).thenReturn(purchase);
        ResponseEntity<?> response = purchaseController.deletePurchase(token, 1);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Compra eliminada correctamente"));
    }

    @Test
    void testConfirmPurchase_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Purchase purchase = new Purchase();
        purchase.setCart(mock(ar.edu.uade.ecommerce.Entity.Cart.class));
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.confirmPurchase(1)).thenReturn(purchase);
        doNothing().when(cartService).confirmProductStock(any());
        ResponseEntity<Purchase> response = purchaseController.confirmPurchase(token, 1, null);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testConfirmPurchase_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = purchaseController.confirmPurchase(token, 1, null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testConfirmPurchase_TokenInvalido() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn(null);
        ResponseEntity<?> response = purchaseController.confirmPurchase(token, 1, null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testConfirmPurchase_EmailNull() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn(null);
        when(authService.getUserByEmail(null)).thenReturn(null);
        ResponseEntity<?> response = purchaseController.confirmPurchase(token, 1, null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testConfirmPurchase_UserNull() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<?> response = purchaseController.confirmPurchase(token, 1, null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testConfirmPurchase_SessionInactive() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<?> response = purchaseController.confirmPurchase(token, 1, null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testConfirmPurchase_PurchaseNull() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.confirmPurchase(1)).thenReturn(null);
        ResponseEntity<?> response = purchaseController.confirmPurchase(token, 1, null);
        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testConfirmPurchase_PurchaseSinCart() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        Purchase purchase = new Purchase();
        purchase.setCart(null);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.confirmPurchase(1)).thenReturn(purchase);
        ResponseEntity<Purchase> response = purchaseController.confirmPurchase(token, 1, null);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testGetMyPurchases() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(purchaseService.getPurchasesByUserEmail("test@email.com")).thenReturn(List.of(new PurchaseInvoiceDTO()));
        ResponseEntity<List<PurchaseInvoiceDTO>> response = purchaseController.getMyPurchases(token);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetPendingCart_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setId(1);
        user.setSessionActive(true);
        Purchase purchase = new Purchase();
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.findLastPendingPurchaseByUserWithinHours(1, 4)).thenReturn(purchase);
        ResponseEntity<Purchase> response = purchaseController.getPendingCart(token);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testGetPendingCart_NotFound() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setId(1);
        user.setSessionActive(true);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.findLastPendingPurchaseByUserWithinHours(1, 4)).thenReturn(null);
        ResponseEntity<Purchase> response = purchaseController.getPendingCart(token);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetPendingCart_TokenInvalido() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn(null);
        ResponseEntity<Purchase> response = purchaseController.getPendingCart(token);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPendingCart_EmailNull() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn(null);
        when(authService.getUserByEmail(null)).thenReturn(null);
        ResponseEntity<Purchase> response = purchaseController.getPendingCart(token);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPendingCart_UserNull() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<Purchase> response = purchaseController.getPendingCart(token);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPendingCart_SessionInactive() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setId(1);
        user.setSessionActive(false);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<Purchase> response = purchaseController.getPendingCart(token);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPurchasesByUser_TokenInvalido() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn(null);
        ResponseEntity<List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO>> response = purchaseController.getPurchasesByUser(token);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPurchasesByUser_EmailNull() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn(null);
        when(authService.getUserByEmail(null)).thenReturn(null);
        ResponseEntity<List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO>> response = purchaseController.getPurchasesByUser(token);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPurchasesByUser_UserNull() {
        String token = "Bearer testtoken";
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(null);
        ResponseEntity<List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO>> response = purchaseController.getPurchasesByUser(token);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPurchasesByUser_SessionInactive() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setId(1);
        user.setSessionActive(false);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        ResponseEntity<List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO>> response = purchaseController.getPurchasesByUser(token);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetPurchasesByUser_SinCompras() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setId(1);
        user.setSessionActive(true);
        when(purchaseService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        when(purchaseService.findByUserId(1)).thenReturn(List.of());
        ResponseEntity<List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO>> response = purchaseController.getPurchasesByUser(token);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}
