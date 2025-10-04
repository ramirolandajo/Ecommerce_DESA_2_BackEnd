package ar.edu.uade.ecommerce.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import java.util.Set;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

@Entity
@Data
@Table(indexes = {
        @Index(name = "idx_category_code", columnList = "category_code", unique = true)
})
public class Category {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column(nullable = false)
    private boolean active;

    // Nuevo: código estable entre microservicios (único). Nullable temporalmente para backfill
    @Column(name = "category_code", unique = true)
    private Integer categoryCode;

    public Category() {
        this.active = true;
    }
}
