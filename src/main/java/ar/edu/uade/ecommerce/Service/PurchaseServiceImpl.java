package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Purchase;
import ar.edu.uade.ecommerce.Entity.Event;
import ar.edu.uade.ecommerce.Entity.Purchase.Status;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
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
            Event event = new Event("PurchaseConfirmed", purchase);
            kafkaMockService.sendEvent(event);
        }
        return purchase;
    }
}
