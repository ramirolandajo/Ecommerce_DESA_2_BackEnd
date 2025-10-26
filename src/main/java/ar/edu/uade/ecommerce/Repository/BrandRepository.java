package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    void deleteAll();
    Optional<Brand> findByName(String name);
    Optional<Brand> findByBrandCode(Integer brandCode);

    Collection<Brand> findByActiveTrue();
}
