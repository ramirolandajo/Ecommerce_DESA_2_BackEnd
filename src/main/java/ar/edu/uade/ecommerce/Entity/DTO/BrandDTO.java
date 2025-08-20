package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

@Data
public class BrandDTO {
    private Long id;
    private String name;

    public BrandDTO(Long id, String nombre) {
        this.id = id;
        this.name = name;
    }
}

