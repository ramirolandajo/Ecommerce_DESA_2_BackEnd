package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.FavouriteProducts;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavouriteProductsRepository extends JpaRepository<FavouriteProducts, Long> {
    Optional<FavouriteProducts> findByUserAndProduct(User user, Product product);
    void deleteByUserAndProduct(User user, Product product);
}

