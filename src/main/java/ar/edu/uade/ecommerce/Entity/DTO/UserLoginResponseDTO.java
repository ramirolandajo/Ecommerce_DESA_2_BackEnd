package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

import java.util.List;

@Data
public class UserLoginResponseDTO {
    private boolean success;
    private String bearer_token;
    private UserBasicDTO user;

    @Data
    public static class UserBasicDTO {
        private Integer id;
        private String name;
        private String lastname;
        private String email;
        private List<?> addresses;
    }
}
