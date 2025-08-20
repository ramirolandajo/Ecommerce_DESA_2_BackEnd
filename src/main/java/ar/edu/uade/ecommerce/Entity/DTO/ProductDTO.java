package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String imageUrl;
    private BrandDTO brand;
    private CategoryDTO category;

    public ProductDTO(Long id, String name, String description, Double price, Integer stock, String image, BrandDTO brand, CategoryDTO category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = image;
        this.brand = brand;
        this.category = category;
    }
}

