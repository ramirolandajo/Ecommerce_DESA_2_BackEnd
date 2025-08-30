package ar.edu.uade.ecommerce.Entity.DTO;

import ar.edu.uade.ecommerce.Controllers.ProductController;
import lombok.Data;
import java.util.List;

@Data
public class ReviewResponse {
    private Integer productId;
    private String productTitle;
    private float promedio;
    private List<ProductController.ReviewDTO> reviews;
}

