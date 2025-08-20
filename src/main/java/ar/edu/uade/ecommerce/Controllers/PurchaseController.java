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
}
