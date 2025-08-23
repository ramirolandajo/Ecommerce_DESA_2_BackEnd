package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

@Data
public class RegisterUserDTO {
    private String name;
    private String lastname;
    private String email;
    private String password;
    private String role;
}

