package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    void deleteAll();
    Optional<Brand> findByName(String name);
}
