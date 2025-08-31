package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private Boolean active;

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
    public CategoryDTO() {}
}
