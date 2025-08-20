package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.CartItem;
import ar.edu.uade.ecommerce.Service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart-items")
public class CartItemController {
    @Autowired
    private CartItemService cartItemService;

    @PostMapping
    public CartItem addCartItem(@RequestBody CartItem cartItem) {
        return cartItemService.addCartItem(cartItem);
    }

    @PutMapping("/{id}")
    public CartItem updateCartItem(@PathVariable Integer id, @RequestBody CartItem cartItem) {
        return cartItemService.updateCartItem(id, cartItem);
    }

    @DeleteMapping("/{id}")
    public void removeCartItem(@PathVariable Integer id) {
        cartItemService.removeCartItem(id);
    }

    @GetMapping("/cart/{cartId}")
    public List<CartItem> getCartItemsByCartId(@PathVariable Integer cartId) {
        return cartItemService.getCartItemsByCartId(cartId);
    }
}
