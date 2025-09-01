package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.ProductView;
import ar.edu.uade.ecommerce.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductViewRepository extends JpaRepository<ProductView, Long> {
    Page<ProductView> findByUser(User user, Pageable pageable);
}

