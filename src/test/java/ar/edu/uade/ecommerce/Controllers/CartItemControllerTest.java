package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.CartItem;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import ar.edu.uade.ecommerce.Service.CartItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartItemControllerTest {
    @Mock
    private CartItemService cartItemService;
    @Mock
    private AuthService authService;
    @InjectMocks
    private CartItemController cartItemController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCartItemsByCartId_Success() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(true);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        List<CartItem> items = List.of(new CartItem(), new CartItem());
        when(cartItemService.getCartItemsByCartId(1)).thenReturn(items);
        List<CartItem> result = cartItemController.getCartItemsByCartId(token, 1);
        assertEquals(2, result.size());
    }

    @Test
    void testGetCartItemsByCartId_Unauthorized() {
        String token = "Bearer testtoken";
        User user = new User();
        user.setSessionActive(false);
        when(authService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(authService.getUserByEmail("test@email.com")).thenReturn(user);
        assertThrows(RuntimeException.class, () -> cartItemController.getCartItemsByCartId(token, 1));
    }
}

