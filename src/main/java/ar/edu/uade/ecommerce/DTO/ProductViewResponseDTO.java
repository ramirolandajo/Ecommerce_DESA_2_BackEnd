package ar.edu.uade.ecommerce.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductViewResponseDTO {
    private Long viewId;
    private Long productId;
    private String productName;
    private List<String> categories;
    private String brand;
    private LocalDateTime viewedAt;

    public ProductViewResponseDTO(Long viewId, Integer productId, String productName, List<String> categories, String brand, LocalDateTime viewedAt) {
        this.viewId = viewId;
        this.productId = Long.valueOf(productId);
        this.productName = productName;
        this.categories = categories;
        this.brand = brand;
        this.viewedAt = viewedAt;
    }

    public Long getViewId() {
        return viewId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public List<String> getCategories() {
        return categories;
    }

    public String getBrand() {
        return brand;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }
}

