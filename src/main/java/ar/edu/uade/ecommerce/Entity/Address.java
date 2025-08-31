package ar.edu.uade.ecommerce.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("address-user")
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        // Compara solo por description y user (por id de user)
        if (description == null) {
            if (address.description != null) return false;
        } else if (!description.equals(address.description)) return false;
        if (user == null || user.getId() == null) {
            return address.user == null || address.user.getId() == null;
        } else {
            return user.getId().equals(address.user.getId());
        }
    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + (user != null && user.getId() != null ? user.getId().hashCode() : 0);
        return result;
    }
}
