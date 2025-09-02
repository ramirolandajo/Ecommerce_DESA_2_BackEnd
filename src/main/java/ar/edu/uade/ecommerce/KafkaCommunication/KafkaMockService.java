package ar.edu.uade.ecommerce.KafkaCommunication;

import ar.edu.uade.ecommerce.Entity.DTO.BrandDTO;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.Entity.DTO.ProductDTO;
import ar.edu.uade.ecommerce.Entity.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaMockService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaMockService.class);

    // Simula el envío de un evento a Kafka
    public void sendEvent(Event event) {
        logger.info("Evento mockeado enviado: {}", event);
    }

    // Simula recibir todos los productos
    public ProductSyncMessage getProductsMock() {
        return new ProductSyncMessage(
            "ProductSync",
            new ProductSyncPayload(List.of(
                new ProductDTO(
                        1L,
                        "Smartphone",
                        "Teléfono inteligente con Android",
                        350.0f,
                        100,
                        List.of("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?q=80&w=1400&auto=format&fit=crop"),
                        new BrandDTO(20L, null, null),
                        List.of(new CategoryDTO(1L, null, null), new CategoryDTO(5L, null, null)),
                        true,
                        false,
                        true,
                        true,
                        true,
                        0f,
                        10.0f,
                        340.0f,
                        1001
                ),
                new ProductDTO(
                        2L,
                        "Laptop",
                        "Notebook ultraliviana",
                        1200.0f,
                        50,
                        List.of("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?q=80&w=1400&auto=format&fit=crop"),
                        new BrandDTO(21L, null, null),
                        List.of(new CategoryDTO(2L, null, null)),
                        false,
                        true,
                        false,
                        true,
                        true,
                        0f,
                        15.0f,
                        1100.0f,
                        1002
                ),
                new ProductDTO(
                        3L,
                        "Auriculares Bluetooth",
                        "Auriculares inalámbricos",
                        80.0f,
                        25,
                        List.of("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?q=80&w=1400&auto=format&fit=crop"),
                        new BrandDTO(23L, null, null),
                        List.of(new CategoryDTO(3L, null, null)),
                        false,
                        false,
                        true,
                        true,
                        true,
                        0f,
                        5.0f,
                        75.0f,
                        1003
                ),
                new ProductDTO(
                        4L,
                        "Smartwatch",
                        "Reloj inteligente resistente al agua",
                        200.0f,
                        12,
                        List.of("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?q=80&w=1400&auto=format&fit=crop"),
                        new BrandDTO(23L, null, null),
                        List.of(new CategoryDTO(4L, null, null)),
                        true,
                        true,
                        false,
                        true,
                        true,
                        0f,
                        20.0f,
                        180.0f,
                        1004
                )
            )),
            java.time.LocalDateTime.now().toString()
        );
    }

    // Clases internas para el mensaje mock
    public static class ProductSyncMessage {
        public String type;
        public ProductSyncPayload payload;
        public String timestamp;
        public ProductSyncMessage(String type, ProductSyncPayload payload, String timestamp) {
            this.type = type;
            this.payload = payload;
            this.timestamp = timestamp;
        }
    }
    public static class ProductSyncPayload {
        public List<ProductDTO> products;
        public ProductSyncPayload(List<ProductDTO> products) {
            this.products = products;
        }
    }

    // Simula recibir todas las categorías
    public CategorySyncMessage getCategoriesMock() {
        return new CategorySyncMessage(
            "CategorySync",
            new CategorySyncPayload(List.of(
                new CategoryDTO(1L, "Celulares", true),
                new CategoryDTO(2L, "Computadoras", true),
                new CategoryDTO(3L, "Audio", true),
                new CategoryDTO(4L, "Wearables", true),
                new CategoryDTO(5L, "Accesorios", true),
                new CategoryDTO(6L, "Tablets", true),
                new CategoryDTO(7L, "Monitores", true),
                new CategoryDTO(8L, "Impresoras", true)
            )),
            java.time.LocalDateTime.now().toString()
        );
    }

    public static class CategorySyncMessage {
        public String type;
        public CategorySyncPayload payload;
        public String timestamp;
        public CategorySyncMessage(String type, CategorySyncPayload payload, String timestamp) {
            this.type = type;
            this.payload = payload;
            this.timestamp = timestamp;
        }
    }
    public static class CategorySyncPayload {
        public List<CategoryDTO> categories;
        public CategorySyncPayload(List<CategoryDTO> categories) {
            this.categories = categories;
        }
    }

    // Simula recibir todas las marcas
    public BrandSyncMessage getBrandsMock() {
        return new BrandSyncMessage(
            "BrandSync",
            new BrandSyncPayload(List.of(
                new BrandDTO(1L, "Samsung", true),
                new BrandDTO(2L, "Dell", true),
                new BrandDTO(3L, "Sony", true),
                new BrandDTO(4L, "Apple", true),
                new BrandDTO(5L, "HP", true),
                new BrandDTO(6L, "Lenovo", true)
            )),
            java.time.LocalDateTime.now().toString()
        );
    }

    public static class BrandSyncMessage {
        public String type;
        public BrandSyncPayload payload;
        public String timestamp;
        public BrandSyncMessage(String type, BrandSyncPayload payload, String timestamp) {
            this.type = type;
            this.payload = payload;
            this.timestamp = timestamp;
        }
    }
    public static class BrandSyncPayload {
        public List<BrandDTO> brands;
        public BrandSyncPayload(List<BrandDTO> brands) {
            this.brands = brands;
        }
    }

    // Simula la recepción de un evento desde otro módulo (inventario)
    public void mockListener(Event event) {
        logger.info("Evento recibido por el listener mock: {}", event);
        // Aquí podrías simular lógica de inventario, por ejemplo actualizar stock
    }
}