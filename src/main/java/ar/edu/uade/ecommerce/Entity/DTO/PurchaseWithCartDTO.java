package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseWithCartDTO {
    private Integer id;
    private LocalDateTime date;
    private LocalDateTime reservationTime;
    private String direction;
    private String status;
    private CartDTO cart;

    @Data
    public static class CartDTO {
        private Integer id;
        private Float finalPrice;
        private List<CartItemDTO> items;
    }

    @Data
    public static class CartItemDTO {
        private Integer id;
        private Integer quantity;
        private ProductDTO product;
    }

    @Data
    public static class ProductDTO {
        private Integer id;
        private String title;
        private String description;
        private Float price;
        private Integer stock;
    }
}

