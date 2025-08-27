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

    @GetMapping
    public List<Purchase> getAllPurchases() {
        return purchaseService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getPurchaseById(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (!user.getSessionActive()) {
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
        if (!user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        Purchase created = purchaseService.save(purchase);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchase(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (!user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        purchaseService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Purchase> confirmPurchase(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (!user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        Purchase purchase = purchaseService.confirmPurchase(id);
        return ResponseEntity.ok(purchase);
    }

    @GetMapping("/my-purchases")
    public ResponseEntity<List<PurchaseInvoiceDTO>> getMyPurchases(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        List<PurchaseInvoiceDTO> invoices = purchaseService.getPurchasesByUserEmail(email);
        return ResponseEntity.ok(invoices);
    }
}
