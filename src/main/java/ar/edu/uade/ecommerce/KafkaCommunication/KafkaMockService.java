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
                ),
                new ProductDTO(
                        5L,
                        "Tablet",
                        "Tablet de 10 pulgadas, ideal para estudiar",
                        400.0f,
                        30,
                        List.of("https://images.unsplash.com/photo-1465101046530-73398c7f28ca?q=80&w=1400&auto=format&fit=crop"),
                        new BrandDTO(22L, null, null),
                        List.of(new CategoryDTO(6L, null, null)),
                        true,
                        false,
                        false,
                        false,
                        true,
                        0f,
                        10.0f,
                        360.0f,
                        1005
                ),
                new ProductDTO(
                        6L,
                        "Monitor LED",
                        "Monitor de 24 pulgadas Full HD",
                        250.0f,
                        20,
                        List.of("https://images.unsplash.com/photo-1519125323398-675f0ddb6308?q=80&w=1400&auto=format&fit=crop"),
                        new BrandDTO(24L, null, null),
                        List.of(new CategoryDTO(7L, null, null)),
                        false,
                        true,
                        false,
                        false,
                        true,
                        0f,
                        5.0f,
                        237.5f,
                        1006
                ),
                new ProductDTO(
                        7L,
                        "Impresora Láser",
                        "Impresora láser monocromática",
                        180.0f,
                        10,
                        List.of("https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=1400&auto=format&fit=crop"),
                        new BrandDTO(25L, null, null),
                        List.of(new CategoryDTO(8L, null, null)),
                        false,
                        false,
                        false,
                        false,
                        false,
                        0f,
                        0.0f,
                        180.0f,
                        1007
                ),
                new ProductDTO(
                        8L,
                        "Mouse inalámbrico",
                        "Mouse ergonómico con batería recargable",
                        35.0f,
                        60,
                        List.of("https://images.unsplash.com/photo-1519125323398-675f0ddb6308?q=80&w=1400&auto=format&fit=crop"),
                        new BrandDTO(26L, null, null),
                        List.of(new CategoryDTO(5L, null, null)),
                        false,
                        false,
                        false,
                        false,
                        true,
                        0f,
                        0.0f,
                        35.0f,
                        1008
                ),
                new ProductDTO(
                        9L,
                        "Teclado mecánico",
                        "Teclado retroiluminado RGB",
                        70.0f,
                        40,
                        List.of("https://images.unsplash.com/photo-1465101046530-73398c7f28ca?q=80&w=1400&auto=format&fit=crop"),
                        new BrandDTO(27L, null, null),
                        List.of(new CategoryDTO(5L, null, null)),
                        true,
                        true,
                        false,
                        false,
                        true,
                        0f,
                        10.0f,
                        63.0f,
                        1009
                ),
                new ProductDTO(
                        10L,
                        "Notebook Gamer",
                        "Notebook con gráfica dedicada y 16GB RAM",
                        2200.0f,
                        8,
                        List.of("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?q=80&w=1400&auto=format&fit=crop"),
                        new BrandDTO(28L, null, null),
                        List.of(new CategoryDTO(2L, null, null)),
                        true,
                        true,
                        true,
                        true,
                        true,
                        0f,
                        20.0f,
                        1760.0f,
                        1010
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
                new CategoryDTO(8L, "Impresoras", true),
                new CategoryDTO(9L, "Redes", true),
                new CategoryDTO(10L, "Almacenamiento", true),
                new CategoryDTO(11L, "Gaming", true),
                new CategoryDTO(12L, "Componentes", true),
                new CategoryDTO(13L, "Smart Home", true),
                new CategoryDTO(14L, "Fotografía", true),
                new CategoryDTO(15L, "Video", true)
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
                new BrandDTO(6L, "Lenovo", true),
                new BrandDTO(7L, "Logitech", true),
                new BrandDTO(8L, "Kingston", true),
                new BrandDTO(9L, "TP-Link", true),
                new BrandDTO(10L, "MSI", true),
                new BrandDTO(11L, "Asus", true),
                new BrandDTO(12L, "Acer", true),
                new BrandDTO(13L, "Xiaomi", true),
                new BrandDTO(14L, "Canon", true),
                new BrandDTO(15L, "GoPro", true)
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

    // Mock para edición simple de producto
    public EditProductSimpleMessage getEditProductMockSimple() {
        return new EditProductSimpleMessage(
            "EditProductSimple",
            new EditProductSimplePayload(
                    31L,
                999, // nuevo stock
                199.99f // nuevo precio
            ),
            java.time.LocalDateTime.now().toString()
        );
    }
    public static class EditProductSimpleMessage {
        public String type;
        public EditProductSimplePayload payload;
        public String timestamp;
        public EditProductSimpleMessage(String type, EditProductSimplePayload payload, String timestamp) {
            this.type = type;
            this.payload = payload;
            this.timestamp = timestamp;
        }
    }
    public static class EditProductSimplePayload {
        public Long id;
        public Integer stock;
        public Float price;
        public EditProductSimplePayload(Long id, Integer stock, Float price) {
            this.id = id;
            this.stock = stock;
            this.price = price;
        }
    }
    // Mock para edición completa de producto
    public EditProductFullMessage getEditProductMockFull() {
        return new EditProductFullMessage(
            "EditProductFull",
            new EditProductFullPayload(
                30,
                "Producto editado desde mock",
                "Descripción editada desde mock",
                123.45f,
                888,
                List.of("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?q=80&w=1400&auto=format&fit=crop"),
                new BrandDTO(1L, "Samsung", true),
                List.of(new CategoryDTO(1L, "Celulares", true)),
                true,
                false,
                true,
                false,
                true,
                4.5f,
                5.0f,
                130.0f,
                1234
            ),
            java.time.LocalDateTime.now().toString()
        );
    }
    public static class EditProductFullMessage {
        public String type;
        public EditProductFullPayload payload;
        public String timestamp;
        public EditProductFullMessage(String type, EditProductFullPayload payload, String timestamp) {
            this.type = type;
            this.payload = payload;
            this.timestamp = timestamp;
        }
    }
    public static class EditProductFullPayload extends ProductDTO {
        public EditProductFullPayload(Integer id, String title, String description, Float price, Integer stock, List<String> mediaSrc, BrandDTO brand, List<CategoryDTO> categories, Boolean isNew, Boolean isBestseller, Boolean isFeatured, Boolean hero, Boolean active, Float calification, Float discount, Float priceUnit, Integer productCode) {
            super(Long.valueOf(id), title, description, price, stock, mediaSrc, brand, categories, isNew, isBestseller, isFeatured, hero, active, calification, discount, priceUnit, productCode);
        }
    }
    // Mock para agregar producto
    public AddProductMessage getAddProductMock() {
        return new AddProductMessage(
            "AddProduct",
            new AddProductPayload(
                new ProductDTO(
                    99L,
                    "Nuevo Producto Mock",
                    "Descripción del producto mockeado",
                    500.0f,
                    50,
                    List.of("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?q=80&w=1400&auto=format&fit=crop"),
                    new BrandDTO(1L, "Samsung", true),
                    List.of(new CategoryDTO(1L, "Celulares", true)),
                    true,
                    false,
                    true,
                    true,
                    true,
                    0f,
                    10.0f,
                    450.0f,
                    9999
                )
            ),
            java.time.LocalDateTime.now().toString()
        );
    }
    public static class AddProductMessage {
        public String type;
        public AddProductPayload payload;
        public String timestamp;
        public AddProductMessage(String type, AddProductPayload payload, String timestamp) {
            this.type = type;
            this.payload = payload;
            this.timestamp = timestamp;
        }
    }
    public static class AddProductPayload {
        public ProductDTO product;
        public AddProductPayload(ProductDTO product) {
            this.product = product;
        }
    }

    // Mock para activar producto
    public ActivateProductMessage getActivateProductMock() {
        return new ActivateProductMessage(
            "ActivateProduct",
            new ActivateProductPayload(29L),
            java.time.LocalDateTime.now().toString()
        );
    }
    public static class ActivateProductMessage {
        public String type;
        public ActivateProductPayload payload;
        public String timestamp;
        public ActivateProductMessage(String type, ActivateProductPayload payload, String timestamp) {
            this.type = type;
            this.payload = payload;
            this.timestamp = timestamp;
        }
    }
    public static class ActivateProductPayload {
        public Long id;
        public ActivateProductPayload(Long id) {
            this.id = id;
        }
    }

    // Mock para desactivar producto
    public DeactivateProductMessage getDeactivateProductMock() {
        return new DeactivateProductMessage(
            "DeactivateProduct",
            new DeactivateProductPayload(29L),
            java.time.LocalDateTime.now().toString()
        );
    }
    public static class DeactivateProductMessage {
        public String type;
        public DeactivateProductPayload payload;
        public String timestamp;
        public DeactivateProductMessage(String type, DeactivateProductPayload payload, String timestamp) {
            this.type = type;
            this.payload = payload;
            this.timestamp = timestamp;
        }
    }
    public static class DeactivateProductPayload {
        public Long id;
        public DeactivateProductPayload(Long id) {
            this.id = id;
        }
    }
}