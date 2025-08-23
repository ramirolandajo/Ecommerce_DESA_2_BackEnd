package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Purchase;
import ar.edu.uade.ecommerce.Service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    @GetMapping
    public List<Purchase> getAllPurchases() {
        return purchaseService.findAll();
    }

    @GetMapping("/{id}")
    public Purchase getPurchaseById(@PathVariable Integer id) {
        return purchaseService.findById(id);
    }

    @PostMapping
    public Purchase createPurchase(@RequestBody Purchase purchase) {
        return purchaseService.save(purchase);
    }

    @DeleteMapping("/{id}")
    public void deletePurchase(@PathVariable Integer id) {
        purchaseService.deleteById(id);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Purchase> confirmPurchase(@PathVariable Integer id) {
        Purchase purchase = purchaseService.confirmPurchase(id);
        return ResponseEntity.ok(purchase);
    }

    @PostMapping("/cart/{cartId}/add-product")
    public ResponseEntity<String> addProductToCart(@PathVariable Integer cartId, @RequestParam Integer productId, @RequestParam int quantity) {
        purchaseService.addProductToCart(cartId, productId, quantity);
        return ResponseEntity.ok("Producto agregado al carrito y evento mockeado");
    }

    @PutMapping("/cart-item/{cartItemId}/edit")
    public ResponseEntity<String> editCartItem(@PathVariable Integer cartItemId, @RequestParam int newQuantity) {
        purchaseService.editCartItem(cartItemId, newQuantity);
        return ResponseEntity.ok("Cantidad de producto editada y evento mockeado");
    }

    @DeleteMapping("/cart-item/{cartItemId}/remove")
    public ResponseEntity<String> removeProductFromCart(@PathVariable Integer cartItemId) {
        purchaseService.removeProductFromCart(cartItemId);
        return ResponseEntity.ok("Producto eliminado del carrito y evento mockeado");
    }
}
