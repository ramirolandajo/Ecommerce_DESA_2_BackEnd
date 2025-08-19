package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    // Métodos personalizados si son necesarios
}

