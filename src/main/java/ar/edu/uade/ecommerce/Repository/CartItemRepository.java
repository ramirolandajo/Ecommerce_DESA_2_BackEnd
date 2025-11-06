package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCartId(Integer cartId);
    // Métodos personalizados si son necesarios
}

