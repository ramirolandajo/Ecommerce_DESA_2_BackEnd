package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

import java.util.List;

@Data
public class SearchProductDTO {
    private Long id;
    private String title;
    private String description;
    private List<String> mediaSrc;

    public SearchProductDTO(Long id, String title, String description, List<String> mediaSrc) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.mediaSrc = mediaSrc;
    }

}

