package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findByUser_Id(Integer userId);

    List<Purchase> findByUser_IdAndStatusOrderByReservationTimeDesc(Integer userId, Purchase.Status status);

    Purchase findByCartId(Integer id);

    // Nuevo: consultar compras por estado cuya reservationTime sea anterior a una fecha
    List<Purchase> findByStatusAndReservationTimeBefore(Purchase.Status status, LocalDateTime time);

    // Nuevo: obtener todas las compras asociadas a un carrito y con un estado específico
    List<Purchase> findByCart_IdAndStatus(Integer cartId, Purchase.Status status);

    // Nuevo: traer purchases expiradas junto con cart y cart.user para evitar N+1
    @Query("select p from Purchase p join fetch p.cart c left join fetch c.user where p.status = :status and p.reservationTime < :time")
    List<Purchase> findExpiredWithCartAndUser(@Param("status") Purchase.Status status, @Param("time") LocalDateTime time);
}
