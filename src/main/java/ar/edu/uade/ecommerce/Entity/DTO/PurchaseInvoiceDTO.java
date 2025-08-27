package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseInvoiceDTO {
    private Integer purchaseId;
    private LocalDateTime purchaseDate;
    private Float totalAmount;
    private List<ProductDetailDTO> products;

    @Data
    public static class ProductDetailDTO {
        private Integer id;
        private String description;
        private Integer stock;
        private Float price;

    }
}

