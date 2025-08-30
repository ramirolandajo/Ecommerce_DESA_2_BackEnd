package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO;
import ar.edu.uade.ecommerce.Entity.Purchase;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import ar.edu.uade.ecommerce.Service.PurchaseService;
import ar.edu.uade.ecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ar.edu.uade.ecommerce.Service.CartService cartService;

    @GetMapping
    public List<Purchase> getAllPurchases() {
        return purchaseService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getPurchaseById(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        Purchase purchase = purchaseService.findById(id);
        return ResponseEntity.ok(purchase);
    }

    @PostMapping
    public ResponseEntity<Purchase> createPurchase(@RequestHeader("Authorization") String authHeader, @RequestBody Purchase purchase) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        // Setear el usuario y el estado PENDING automáticamente
        purchase.setUser(user);
        purchase.setStatus(Purchase.Status.PENDING);
        Purchase created = purchaseService.save(purchase);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}") //cancelo la compra por tiempo de vida de la compra (4 horas) o manualmente se cancela
    public ResponseEntity<?> deletePurchase(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        Purchase purchase = purchaseService.findById(id);
        if (purchase != null && purchase.getCart() != null) {
            cartService.revertProductStock(purchase.getCart());
        }
        purchaseService.deleteById(id);
        return ResponseEntity.ok("Compra eliminada correctamente. ID: " + id);
    }

    @PostMapping("/{id}/confirm") //confirmo la compra y genero la factura
    public ResponseEntity<Purchase> confirmPurchase(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        Purchase purchase = purchaseService.confirmPurchase(id);
        if (purchase != null && purchase.getCart() != null) {
            // Confirmar el stock y enviar evento por Kafka
            cartService.confirmProductStock(purchase.getCart());
        }
        return ResponseEntity.ok(purchase);
    }

    @GetMapping("/my-purchases")
    public ResponseEntity<List<PurchaseInvoiceDTO>> getMyPurchases(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        List<PurchaseInvoiceDTO> invoices = purchaseService.getPurchasesByUserEmail(email);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/pending-cart")
    public ResponseEntity<Purchase> getPendingCart(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        // Buscar la última compra pendiente del usuario cuyo tiempo de vida sea menor a 4 horas
        Purchase pending = purchaseService.findLastPendingPurchaseByUserWithinHours(user.getId(), 4);
        if (pending == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/user-purchases")
    public ResponseEntity<List<Purchase>> getPurchasesByUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        List<Purchase> purchases = purchaseService.findByUserId(user.getId());
        return ResponseEntity.ok(purchases);
    }
}
