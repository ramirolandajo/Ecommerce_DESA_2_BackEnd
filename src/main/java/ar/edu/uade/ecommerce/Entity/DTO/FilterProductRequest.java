package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

@Data
public class FilterProductRequest {
    private Long categoryId;
    private Long brandId;
    private Float priceMin;
    private Float priceMax;
    private Integer brandCode;
    private Integer categoryCode;
    private java.util.List<Integer> brandCodes;
    private java.util.List<Integer> categoryCodes;
    private String sortBy; // "price" o "relevance"
    private String sortOrder; // "asc" o "desc"
    private Integer page;
    private Integer size;

}
