package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.Token;
import ar.edu.uade.ecommerce.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    Optional<Token> findByToken(String token);
    Optional<Token> findByUser(User user);
    void deleteByToken(String token);
}

