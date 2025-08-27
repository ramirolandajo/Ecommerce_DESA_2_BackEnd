package ar.edu.uade.ecommerce.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    @JsonBackReference("brand-product")
    private Brand brand;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private float price;

    @Column
    @ElementCollection
    private List<String> mediaSrc; // solo los links de las imágenes

    @ManyToMany
    @JoinTable(
        name = "product_category",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonIgnore
    private Set<Category> categories;

    @Column
    private boolean isNew;
    @Column
    private boolean isBestseller;
    @Column
    private boolean isFeatured;
    @Column
    private int stock;
    @Column
    private boolean hero;
}
