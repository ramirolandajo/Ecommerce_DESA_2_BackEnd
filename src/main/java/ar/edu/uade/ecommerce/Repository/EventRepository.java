package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
}

