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
                new ProductDTO(1L, "Smartphone", "Teléfono inteligente con Android", 350.0, 15, "img/smartphone.png",
                        new BrandDTO(1L, "Samsung"), new CategoryDTO(1L, "Celulares")),
                new ProductDTO(2L, "Laptop", "Notebook ultraliviana", 1200.0, 7, "img/laptop.png",
                        new BrandDTO(2L, "Dell"), new CategoryDTO(2L, "Computadoras")),
                new ProductDTO(3L, "Auriculares Bluetooth", "Auriculares inalámbricos", 80.0, 25, "img/auriculares.png",
                        new BrandDTO(3L, "Sony"), new CategoryDTO(3L, "Audio")),
                new ProductDTO(4L, "Smartwatch", "Reloj inteligente resistente al agua", 200.0, 12, "img/smartwatch.png",
                        new BrandDTO(4L, "Apple"), new CategoryDTO(4L, "Wearables"))
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