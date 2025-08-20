package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Purchase;
import java.util.List;

public interface PurchaseService {
    Purchase save(Purchase purchase);
    Purchase findById(Integer id);
    List<Purchase> findAll();
    void deleteById(Integer id);
    Purchase confirmPurchase(Integer id);
}
