package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Cart;
import ar.edu.uade.ecommerce.Entity.CartItem;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartControllerTest {
    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCartById_Success() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findById(1)).thenReturn(cart);
        ResponseEntity<Cart> response = cartController.getCartById(token, 1);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(cart, response.getBody());
    }

    @Test
    void testGetCartById_Unauthorized() {
        String token = "Bearer testtoken";
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(false);
        ResponseEntity<Cart> response = cartController.getCartById(token, 1);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testGetCartById_NotFound() {
        String token = "Bearer testtoken";
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findById(1)).thenReturn(null);
        ResponseEntity<Cart> response = cartController.getCartById(token, 1);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCreateCart_Success() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        User user = new User();
        user.setSessionActive(true);
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_Unauthorized() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(false);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testDeleteCart_Success() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findById(1)).thenReturn(cart);
        doNothing().when(cartService).deleteCart(1);
        ResponseEntity<Void> response = cartController.deleteCart(token, 1);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteCart_Unauthorized() {
        String token = "Bearer testtoken";
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(false);
        ResponseEntity<Void> response = cartController.deleteCart(token, 1);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testDeleteCart_NotFound() {
        String token = "Bearer testtoken";
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findById(1)).thenReturn(null);
        ResponseEntity<Void> response = cartController.deleteCart(token, 1);
        assertEquals(404, response.getStatusCodeValue());
    }
}

