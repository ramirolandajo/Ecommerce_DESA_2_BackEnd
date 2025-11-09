package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Entity.ProductView;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.ProductViewServiceImpl;
import ar.edu.uade.ecommerce.Entity.DTO.ProductViewResponseDTO;
import ar.edu.uade.ecommerce.Service.AuthService;
import ar.edu.uade.ecommerce.Service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-views")
public class ProductViewController {
    private static final Logger logger = LoggerFactory.getLogger(ProductViewController.class);

    @Autowired
    private ProductViewServiceImpl productViewServiceImpl;
    @Autowired
    private ProductService productService;
    @Autowired
    private AuthService authService;

    @Autowired
    private ar.edu.uade.ecommerce.messaging.ECommerceEventService ecommerceEventService;

    // POST: Registrar vista de producto
    @PostMapping("/{id}")
    public ResponseEntity<?> registerProductView(@RequestHeader("Authorization") String authHeader,
                                                 @PathVariable("id") Long productId) {
        if (authHeader == null) {
            return ResponseEntity.status(401).body("Usuario no logueado");
        }
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).body("Usuario no logueado");
        }
        Product product = productService.findById(productId);
        if (product == null) {
            return ResponseEntity.status(404).body("Producto no encontrado");
        }
        ProductView view = productViewServiceImpl.saveProductView(user, product);
        String msg = String.format("El usuario %s vio el producto '%s' (ID: %d) en %s", user.getEmail(), product.getTitle(), product.getId(), view.getViewedAt());
        return ResponseEntity.ok(msg);
    }

    // GET: Consultar vistas paginadas
    @GetMapping
    public ResponseEntity<Page<ProductViewResponseDTO>> getProductViews(@RequestHeader("Authorization") String authHeader,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size) {
        if (authHeader == null) {
            return ResponseEntity.status(401).body(Page.empty());
        }
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).body(Page.empty());
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductViewResponseDTO> views = productViewServiceImpl.getProductViewsByUser(user, pageable);

        // Construir el mensaje como lista de productos simple
        List<Map<String,Object>> summaries = productViewServiceImpl.getAllViewsSummary();
        List<Map<String,Object>> productsForEvent = summaries.stream().map(s -> {
            Map<String,Object> m = new HashMap<>();
            m.put("nombre", s.get("productTitle"));
            Object code = s.get("productCode");
            m.put("productCode", code != null ? String.valueOf(code) : null);
            return m;
        }).toList();

        // Envolver en objeto {"views": [...] } para cumplir con el esquema del middleware
        Map<String,Object> payload = new HashMap<>();
        payload.put("views", productsForEvent);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            String jsonKafkaMessage = mapper.writeValueAsString(payload);
            logger.info("[ViewsDaily][GET] Payload que se enviará por evento (objeto):\n{}", jsonKafkaMessage);
            ecommerceEventService.emitRawEvent("GET: Vista diaria de productos", payload);
        } catch (JsonProcessingException e) {
            logger.error("Error al serializar el mensaje Kafka: {}", e.getMessage(), e);
        }
        return ResponseEntity.ok(views);
    }

    // Endpoint para emitir manualmente la vista diaria (útil para pruebas manuales desde Insomnia)
    @PostMapping("/emit-daily")
    @Transactional(readOnly = true)
    public ResponseEntity<String> emitDailyProductViewNow() {
        List<Map<String,Object>> summaries = productViewServiceImpl.getAllViewsSummary();
        List<Map<String,Object>> productsForEvent = summaries.stream().map(s -> {
            Map<String,Object> m = new HashMap<>();
            m.put("nombre", s.get("productTitle"));
            Object code = s.get("productCode");
            m.put("productCode", code != null ? String.valueOf(code) : null);
            return m;
        }).toList();

        Map<String,Object> payload = new HashMap<>();
        payload.put("views", productsForEvent);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            String json = mapper.writeValueAsString(payload);
            ecommerceEventService.emitRawEvent("GET: Vista diaria de productos", payload);
            logger.info("Emisión manual de vista diaria enviada ({} ítems)", productsForEvent.size());
            return ResponseEntity.ok("Evento enviado");
        } catch (Exception ex) {
            logger.error("Error serializando/enviando resumen de product views: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Error enviando evento: " + ex.getMessage());
        }
    }
}
