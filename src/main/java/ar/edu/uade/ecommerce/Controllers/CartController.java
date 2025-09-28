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
    public ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> createCart(@RequestHeader("Authorization") String authHeader, @RequestBody Cart cart) {
        String token = authHeader.replace("Bearer ", "");
        String email = cartService.getEmailFromToken(token);
        if (!cartService.isUserSessionActive(email)) {
            return ResponseEntity.status(401).build();
        }
        ar.edu.uade.ecommerce.Entity.User user = cartService.findUserByEmail(email);
        cart.setUser(user);
        if (cart.getItems() != null) {
            for (ar.edu.uade.ecommerce.Entity.CartItem item : cart.getItems()) {
                item.setCart(cart);
            }
        }
        float finalPrice = 0f;
        if (cart.getItems() != null) {
            for (ar.edu.uade.ecommerce.Entity.CartItem item : cart.getItems()) {
                if (item.getProduct() != null && item.getQuantity() != null) {
                    Float price = item.getProduct().getPrice();
                    if (price != null) {
                        finalPrice += price * item.getQuantity();
                    }
                }
            }
        }
        cart.setFinalPrice(finalPrice);
        Cart createdCart = cartService.createCart(cart);
        boolean stockError = false;
        boolean kafkaError = false;
        // Solo si createdCart no es null, realizar lógica de stock y Kafka
        if (createdCart != null && createdCart.getItems() != null) {
            for (ar.edu.uade.ecommerce.Entity.CartItem item : createdCart.getItems()) {
                ar.edu.uade.ecommerce.Entity.Product product = item.getProduct();
                if (product != null && item.getQuantity() != null) {
                    ar.edu.uade.ecommerce.Entity.Product realProduct = cartService.getProductById(product.getId());
                    if (realProduct != null) {
                        int nuevoStock = realProduct.getStock() - item.getQuantity();
                        realProduct.setStock(nuevoStock);
                        try {
                            cartService.updateProductStock(realProduct);
                        } catch (Exception e) {
                            stockError = true;
                        }
                        item.setProduct(realProduct);
                    }
                }
            }
            try {
                cartService.sendKafkaEvent("POST: Compra pendiente", createdCart);
            } catch (Exception e) {
                kafkaError = true;
            }
        }
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        purchase.setUser(user);
        purchase.setCart(createdCart);
        purchase.setStatus(ar.edu.uade.ecommerce.Entity.Purchase.Status.PENDING);
        purchase.setDate(java.time.LocalDateTime.now());
        purchase.setReservationTime(java.time.LocalDateTime.now());
        ar.edu.uade.ecommerce.Entity.Purchase createdPurchase = cartService.createPurchase(purchase);
        // Si hubo error de stock o kafka, incluir info en el header
        if (stockError || kafkaError) {
            return ResponseEntity.ok().header("X-Stock-Error", String.valueOf(stockError)).header("X-Kafka-Error", String.valueOf(kafkaError)).body(createdPurchase);
        }
        return ResponseEntity.ok(createdPurchase);
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
