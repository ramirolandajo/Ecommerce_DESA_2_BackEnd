package ar.edu.uade.ecommerce.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.uade.ecommerce.Entity.Cart;
import ar.edu.uade.ecommerce.Service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/{id}")
    public ResponseEntity<Cart> getCartById(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = cartService.getEmailFromToken(token);
        if (!cartService.isUserSessionActive(email)) {
            return ResponseEntity.status(401).build();
        }
        Cart cart = cartService.findById(id);
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cart);
    }

    @PostMapping
    public ResponseEntity<Cart> createCart(@RequestHeader("Authorization") String authHeader, @RequestBody Cart cart) {
        String token = authHeader.replace("Bearer ", "");
        String email = cartService.getEmailFromToken(token);
        if (!cartService.isUserSessionActive(email)) {
            return ResponseEntity.status(401).build();
        }
        Cart created = cartService.save(cart);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cart> updateCart(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id, @RequestBody Cart cart) {
        String token = authHeader.replace("Bearer ", "");
        String email = cartService.getEmailFromToken(token);
        if (!cartService.isUserSessionActive(email)) {
            return ResponseEntity.status(401).build();
        }
        Cart existing = cartService.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        // Actualiza los campos necesarios del carrito
        existing.setFinalPrice(cart.getFinalPrice());
        // Si quieres permitir cambiar el usuario, descomenta la siguiente línea:
        // existing.setUser(cart.getUser());
        Cart updated = cartService.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = cartService.getEmailFromToken(token);
        if (!cartService.isUserSessionActive(email)) {
            return ResponseEntity.status(401).build();
        }
        Cart existing = cartService.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        cartService.deleteCart(id);
        return ResponseEntity.ok().build();
    }
}
