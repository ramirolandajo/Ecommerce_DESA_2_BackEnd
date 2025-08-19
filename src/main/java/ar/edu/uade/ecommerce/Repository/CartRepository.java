package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    // Métodos personalizados si son necesarios
}

