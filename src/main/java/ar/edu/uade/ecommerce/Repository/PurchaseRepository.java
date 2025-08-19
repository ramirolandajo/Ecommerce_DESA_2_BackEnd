package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    // Métodos personalizados si son necesarios
}
