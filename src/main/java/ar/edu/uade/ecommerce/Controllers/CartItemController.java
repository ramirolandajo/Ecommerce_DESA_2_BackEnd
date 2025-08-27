package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.CartItem;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import ar.edu.uade.ecommerce.Service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart-items")
public class CartItemController {
    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private AuthService authService;

    @GetMapping("/cart/{cartId}")
    public List<CartItem> getCartItemsByCartId(@RequestHeader("Authorization") String authHeader, @PathVariable Integer cartId) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (!user.getSessionActive()) {
            throw new RuntimeException("Usuario no logueado");
        }
        return cartItemService.getCartItemsByCartId(cartId);
    }
}
