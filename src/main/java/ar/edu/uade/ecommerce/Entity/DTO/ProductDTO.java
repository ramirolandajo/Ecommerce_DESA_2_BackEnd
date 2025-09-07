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

    public static ProductDTO fromEntity(ar.edu.uade.ecommerce.Entity.Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId() != null ? Long.valueOf(product.getId()) : null);
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setMediaSrc(product.getMediaSrc());
        dto.setCalification(product.getCalification());
        dto.setDiscount(product.getDiscount());
        dto.setPriceUnit(product.getPriceUnit());
        dto.setProductCode(product.getProductCode());
        dto.setActive(product.getActive());
        // Marca
        if (product.getBrand() != null) {
            Long brandId = null;
            try {
                brandId = product.getBrand().getId() != null ? Long.valueOf(product.getBrand().getId()) : null;
            } catch (Exception e) {
                brandId = null;
            }
            dto.setBrand(new BrandDTO(brandId, product.getBrand().getName(), product.getBrand().isActive()));
        } else {
            dto.setBrand(null);
        }
        // Categorías
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            dto.setCategories(product.getCategories().stream()
                .map(cat -> {
                    Long catId = cat.getId() != null ? Long.valueOf(cat.getId()) : null;
                    return new CategoryDTO(catId, cat.getName(), cat.isActive());
                })
                .toList());
        } else {
            dto.setCategories(null);
        }
        dto.setIsNew(product.getIsNew());
        dto.setIsBestseller(product.isIsBestseller());
        dto.setIsFeatured(product.isIsFeatured());
        dto.setHero(product.isHero());
        return dto;
    }
}
