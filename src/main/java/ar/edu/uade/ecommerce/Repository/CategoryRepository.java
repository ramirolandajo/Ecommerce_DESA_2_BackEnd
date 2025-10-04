package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByName(String name);
    Optional<Category> findByCategoryCode(Integer categoryCode);
}
