package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findByUser_Id(Integer userId);

    List<Purchase> findByUser_IdAndStatusOrderByReservationTimeDesc(Integer userId, Purchase.Status status);
}
