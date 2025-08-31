package ar.edu.uade.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EcommerceApplicationTests {
    @Test
    void contextLoads() {
        // Test básico para verificar que el contexto de Spring Boot carga correctamente
    }

    @Test
    void mainMethodRuns() {
        EcommerceApplication.main(new String[]{});
    }
}
