package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Integer id;
    private String name;
    private String lastname;
    private String email;
    private String role;
}

