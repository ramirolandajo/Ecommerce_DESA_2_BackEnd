package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Purchase;
import ar.edu.uade.ecommerce.Entity.Event;
import ar.edu.uade.ecommerce.Entity.Purchase.Status;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PurchaseServiceImpl implements PurchaseService {
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private KafkaMockService kafkaMockService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Purchase save(Purchase purchase) {
        return purchaseRepository.save(purchase);
    }

    @Override
    public Purchase findById(Integer id) {
        Optional<Purchase> purchase = purchaseRepository.findById(id);
        return purchase.orElse(null);
    }

    @Override
    public List<Purchase> findAll() {
        return purchaseRepository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        purchaseRepository.deleteById(id);
    }

    @Override
    public Purchase confirmPurchase(Integer id) {
        Purchase purchase = findById(id);
        if (purchase != null) {
            purchase.setStatus(Status.CONFIRMED);
            purchaseRepository.save(purchase);
            try {
                String json = objectMapper.writeValueAsString(purchase);
                Event event = new Event("PurchaseConfirmed", json);
                kafkaMockService.sendEvent(event);
                // Simula la recepción del evento en el módulo de inventario
                kafkaMockService.mockListener(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return purchase;
    }

    @Override
    public void addProductToCart(Integer cartId, Integer productId, int quantity) {
        // Lógica para agregar producto al carrito
        // ...
        Event event = new Event("ProductAddedToCart", "CartId: " + cartId + ", ProductId: " + productId + ", Quantity: " + quantity);
        kafkaMockService.sendEvent(event);
        kafkaMockService.mockListener(event);
    }

    @Override
    public void editCartItem(Integer cartItemId, int newQuantity) {
        // Lógica para editar cantidad de producto en el carrito
        // ...
        Event event = new Event("CartItemEdited", "CartItemId: " + cartItemId + ", NewQuantity: " + newQuantity);
        kafkaMockService.sendEvent(event);
        kafkaMockService.mockListener(event);
    }

    @Override
    public void removeProductFromCart(Integer cartItemId) {
        // Lógica para eliminar producto del carrito
        // ...
        Event event = new Event("ProductRemovedFromCart", "CartItemId: " + cartItemId);
        kafkaMockService.sendEvent(event);
        kafkaMockService.mockListener(event);
    }
}
