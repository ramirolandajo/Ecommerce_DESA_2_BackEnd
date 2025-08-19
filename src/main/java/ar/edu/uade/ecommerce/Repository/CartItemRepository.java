package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    // Métodos personalizados si son necesarios
}

