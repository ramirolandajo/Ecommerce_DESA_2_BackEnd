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
    public List<ProductDTO> getProductsMock() {
        return List.of(
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
        );
    }

    // Simula recibir todas las categorías
    public List<CategoryDTO> getCategoriesMock() {
        return List.of(
                new CategoryDTO(1L, "Celulares"),
                new CategoryDTO(2L, "Computadoras"),
                new CategoryDTO(3L, "Audio"),
                new CategoryDTO(4L, "Wearables"),
                new CategoryDTO(5L, "Accesorios"),
                new CategoryDTO(6L, "Tablets"),
                new CategoryDTO(7L, "Monitores"),
                new CategoryDTO(8L, "Impresoras")
        );
    }

    // Simula recibir todas las marcas
    public List<BrandDTO> getBrandsMock() {
        return List.of(
                new BrandDTO(1L, "Samsung"),
                new BrandDTO(2L, "Dell"),
                new BrandDTO(3L, "Sony"),
                new BrandDTO(4L, "Apple"),
                new BrandDTO(5L, "HP"),
                new BrandDTO(6L, "Lenovo")
        );
    }

    // Simula la recepción de un evento desde otro módulo (inventario)
    public void mockListener(Event event) {
        logger.info("Evento recibido por el listener mock: {}", event);
        // Aquí podrías simular lógica de inventario, por ejemplo actualizar stock
    }
}