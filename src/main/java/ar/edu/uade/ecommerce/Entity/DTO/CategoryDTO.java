package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private Boolean active;
    private Integer categoryCode;

    public CategoryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
        this.active = true;
    }
    public CategoryDTO(Long id, String name, Boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }
    public CategoryDTO(Long id, String name, Boolean active, Integer categoryCode) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.categoryCode = categoryCode;
    }
    public CategoryDTO() {}
}
