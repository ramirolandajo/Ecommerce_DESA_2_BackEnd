package ar.edu.uade.ecommerce.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.OneToMany;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

@Entity
@Data
@Table(indexes = {
        @Index(name = "idx_brand_code", columnList = "brand_code", unique = true)
})
public class Brand {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column(nullable = false)
    private boolean active;

    // Nuevo: código estable entre microservicios (único). Nullable temporalmente para backfill
    @Column(name = "brand_code", unique = true)
    private Integer brandCode;

    @OneToMany(mappedBy = "brand")
    @JsonManagedReference("brand-product")
    private List<Product> products;

    public Brand() {
        this.active = true;
    }
}
