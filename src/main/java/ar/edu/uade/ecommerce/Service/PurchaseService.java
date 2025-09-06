package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO;
import ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO;
import ar.edu.uade.ecommerce.Entity.Purchase;
import java.util.List;

public interface PurchaseService {
    Purchase save(Purchase purchase);
    Purchase findById(Integer id);
    List<Purchase> findAll();
    void deleteById(Integer id);
    Purchase confirmPurchase(Integer id);
    void addProductToCart(Integer cartId, Integer productId, int quantity);
    void editCartItem(Integer cartItemId, int newQuantity);
    void removeProductFromCart(Integer cartItemId);
    String getEmailFromToken(String token);
    List<PurchaseInvoiceDTO> getPurchasesByUserEmail(String email);

    Purchase findLastPendingPurchaseByUserWithinHours(Integer userId, int hours);

    List<Purchase> findByUserId(Integer id);
    List<PurchaseWithCartDTO> getPurchasesWithCartByUserId(Integer userId);
}
