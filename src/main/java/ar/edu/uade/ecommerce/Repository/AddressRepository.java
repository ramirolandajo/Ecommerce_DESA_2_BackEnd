package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    // Métodos personalizados si son necesarios
}

