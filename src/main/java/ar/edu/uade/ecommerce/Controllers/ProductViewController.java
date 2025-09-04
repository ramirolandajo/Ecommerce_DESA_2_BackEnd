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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/api/product-views")
public class ProductViewController {
    @Autowired
    private ProductViewServiceImpl productViewServiceImpl;
    @Autowired
    private ProductService productService;
    @Autowired
    private AuthService authService;
    @Autowired
    private ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService kafkaMockService;

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


        // Construir el mensaje Kafka simulado para este usuario y página como JSON
        List<Object> kafkaViews = Collections.singletonList(views.stream().map(view -> {
            return new LinkedHashMap<String, Object>() {{
                put("productId", view.getProductId());
                put("productName", view.getProductName());
                put("viewedAt", view.getViewedAt());
                put("categories", view.getCategories());
                put("brand", view.getBrand());
            }};
        }).toList());
        java.util.Map<String, Object> kafkaMessage = new java.util.LinkedHashMap<>();
        kafkaMessage.put("userEmail", user.getEmail());
        kafkaMessage.put("views", kafkaViews);
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            String jsonKafkaMessage = mapper.writeValueAsString(kafkaMessage);
            System.out.println("Mensaje que se enviaría por Kafka (JSON):\n" + jsonKafkaMessage);
        } catch (JsonProcessingException e) {
            System.out.println("Error al serializar el mensaje Kafka: " + e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.ok(views);
    }

    // Evento programado cada 24 horas
    @Scheduled(fixedRate = 86400000) // 24 horas en milisegundos
    public void sendDailyProductViewEvent() {
        List<ProductView> allViews = productViewServiceImpl.getAllViews();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < allViews.size(); i++) {
            ProductView view = allViews.get(i);
            sb.append(String.format("{id: %d, nombre: '%s'}",
                view.getProduct().getId(),
                view.getProduct().getTitle()
            ));
            if (i < allViews.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        kafkaMockService.sendEvent(new ar.edu.uade.ecommerce.Entity.Event(
            "DAILY_PRODUCT_VIEWS",
            sb.toString()
        ));
        System.out.println("Mensaje enviado a Kafka (core de mensajería):\n" + sb.toString());
    }
}
