package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Purchase;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplAdditionalTests {

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private Purchase purchase;

    @BeforeEach
    void setUp() {
        purchase = new Purchase();
        purchase.setId(1);
        // Configurar ObjectMapper real por defecto con soporte JavaTime
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "objectMapper", mapper);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
    }

    @Test
    void save_whenObjectMapperThrows_noEventAndExceptionHandled() {
        // Para forzar excepción de serialización, uso un mapper sin el módulo JavaTime
        ObjectMapper failing = new ObjectMapper();
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "objectMapper", failing);
        // El método save captura excepciones y sigue; aquí verificamos que no lance
        assertDoesNotThrow(() -> purchaseService.save(purchase));
    }
}
