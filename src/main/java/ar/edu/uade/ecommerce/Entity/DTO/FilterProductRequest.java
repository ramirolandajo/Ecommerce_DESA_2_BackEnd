package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

@Data
public class FilterProductRequest {
    private Long categoryId;
    private Long brandId;
    private Float priceMin;
    private Float priceMax;

}

