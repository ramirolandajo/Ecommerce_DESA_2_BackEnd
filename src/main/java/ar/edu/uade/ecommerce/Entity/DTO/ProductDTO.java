package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String title;
    private String description;
    private Float price;
    private Integer stock;
    private List<String> mediaSrc;
    private BrandDTO brand;
    private List<CategoryDTO> categories;
    private Boolean isNew;
    private Boolean isBestseller;
    private Boolean isFeatured;
    private Boolean hero;
    private Float calification;
    private Float discount;
    private Float priceUnit;
    private Integer productCode;

    private Boolean active;

    public ProductDTO() {
        // No inicializar active aquí, debe ser null por defecto
    }

    public ProductDTO(Long id, String title, String description, Float price, Integer stock, List<String> mediaSrc, BrandDTO brand, List<CategoryDTO> categories, Boolean isNew, Boolean isBestseller, Boolean isFeatured, Boolean hero, Boolean active, Float calification, Float discount, Float priceUnit, Integer productCode) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.mediaSrc = mediaSrc;
        this.brand = brand;
        this.categories = categories;
        this.isNew = isNew;
        this.isBestseller = isBestseller;
        this.isFeatured = isFeatured;
        this.hero = hero;
        this.active = active;
        this.calification = calification;
        this.discount = discount;
        this.priceUnit = priceUnit;
        this.productCode = productCode;
    }

    public Float getCalification() { return calification; }
    public void setCalification(Float calification) { this.calification = calification; }
    public Float getDiscount() { return discount; }
    public void setDiscount(Float discount) { this.discount = discount; }
    public Float getPriceUnit() { return priceUnit; }
    public void setPriceUnit(Float priceUnit) { this.priceUnit = priceUnit; }
    public Integer getProductCode() { return productCode; }
    public void setProductCode(Integer productCode) { this.productCode = productCode; }
    public void setActive(Boolean active) {
        this.active = active;
    }
}
