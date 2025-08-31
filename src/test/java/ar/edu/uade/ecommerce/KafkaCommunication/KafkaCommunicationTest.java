package ar.edu.uade.ecommerce.KafkaCommunication;

import org.junit.jupiter.api.Test;
import ar.edu.uade.ecommerce.Entity.Event;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Entity.DTO.ProductDTO;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.Entity.DTO.BrandDTO;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class KafkaCommunicationTest {
    @Test
    void testSendEvent() {
        KafkaMockService service = new KafkaMockService();
        Event event = new Event("TestEvent", "payload");
        assertDoesNotThrow(() -> service.sendEvent(event));
    }

    @Test
    void testGetProductsMock() {
        KafkaMockService service = new KafkaMockService();
        List<ProductDTO> products = service.getProductsMock();
        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    void testGetCategoriesMock() {
        KafkaMockService service = new KafkaMockService();
        List<CategoryDTO> categories = service.getCategoriesMock();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
    }

    @Test
    void testGetBrandsMock() {
        KafkaMockService service = new KafkaMockService();
        List<BrandDTO> brands = service.getBrandsMock();
        assertNotNull(brands);
        assertFalse(brands.isEmpty());
    }

    @Test
    void testMockListener() {
        KafkaMockService service = new KafkaMockService();
        Event event = new Event("ListenerEvent", "payload");
        assertDoesNotThrow(() -> service.mockListener(event));
    }
}
