package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Entity.Purchase.Status;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO;
import ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO.ProductDetailDTO;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PurchaseServiceImpl implements PurchaseService {
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private KafkaMockService kafkaMockService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AuthService authService;

    @Autowired
    private ProductService productService;

    private static final Logger logger = LoggerFactory.getLogger(PurchaseServiceImpl.class);

    @Override
    public Purchase save(Purchase purchase) {
        // Si la compra pasa a PENDING, setear reservationTime
        if (purchase.getStatus() == Status.PENDING && purchase.getReservationTime() == null) {
            purchase.setReservationTime(LocalDateTime.now());
            // Avisar por Kafka que se debe reservar el stock
            try {
                String json = objectMapper.writeValueAsString(purchase);
                Event event = new Event("ReserveStock", json);
                kafkaMockService.sendEvent(event);
                kafkaMockService.mockListener(event); // Mock de recepción
            } catch (Exception e) {
                logger.error("Error en reserva de stock", e);
            }
        }
        return purchaseRepository.save(purchase);
    }

    @Override
    public Purchase findById(Integer id) {
        Optional<Purchase> purchase = purchaseRepository.findById(id);
        return purchase.orElse(null);
    }

    @Override
    public List<Purchase> findAll() {
        return purchaseRepository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        purchaseRepository.deleteById(id);
    }

    @Override
    public Purchase confirmPurchase(Integer id) {
        Purchase purchase = findById(id);
        if (purchase != null) {
            purchase.setStatus(Status.CONFIRMED);
            purchaseRepository.save(purchase);
            try {
                String json = objectMapper.writeValueAsString(purchase);
                Event event = new Event("PurchaseConfirmed", json);
                kafkaMockService.sendEvent(event);
                // Simula la recepción del evento en el módulo de inventario
                kafkaMockService.mockListener(event);
            } catch (Exception e) {
                logger.error("Error al confirmar compra", e);
            }
        }
        return purchase;
    }

    @Override
    public void addProductToCart(Integer cartId, Integer productId, int quantity) {
        // Lógica para agregar producto al carrito
        // ...
        Event event = new Event("ProductAddedToCart", "CartId: " + cartId + ", ProductId: " + productId + ", Quantity: " + quantity);
        kafkaMockService.sendEvent(event);
        kafkaMockService.mockListener(event);
    }

    @Override
    public void editCartItem(Integer cartItemId, int newQuantity) {
        // Lógica para editar cantidad de producto en el carrito
        // ...
        Event event = new Event("CartItemEdited", "CartItemId: " + cartItemId + ", NewQuantity: " + newQuantity);
        kafkaMockService.sendEvent(event);
        kafkaMockService.mockListener(event);
    }

    @Override
    public void removeProductFromCart(Integer cartItemId) {
        // Lógica para eliminar producto del carrito
        // ...
        Event event = new Event("ProductRemovedFromCart", "CartItemId: " + cartItemId);
        kafkaMockService.sendEvent(event);
        kafkaMockService.mockListener(event);
    }

    @Scheduled(fixedRate = 60000) // Cada 1 minuto
    public void releaseExpiredReservations() {
        List<Purchase> pendingPurchases = purchaseRepository.findAll().stream()
                .filter(p -> p.getStatus() == Status.PENDING && p.getReservationTime() != null)
                .toList();
        for (Purchase purchase : pendingPurchases) {
            if (purchase.getReservationTime().plusHours(4).isBefore(LocalDateTime.now())) {
                // Avisar por Kafka que se debe liberar el stock
                try {
                    String json = objectMapper.writeValueAsString(purchase);
                    Event event = new Event("ReleaseStock", json);
                    kafkaMockService.sendEvent(event);
                    kafkaMockService.mockListener(event); // Mock de recepción
                } catch (Exception e) {
                    logger.error("Error al liberar reserva", e);
                }
                purchase.setStatus(Status.CANCELLED);
                purchaseRepository.save(purchase);
            }
        }
    }

    public String mockStockChange(Integer productId, int newStock) {
        Product updatedProduct = productService.updateProductStock(productId, newStock);
        return "Cambio detectado de stock. Producto actualizado: " + updatedProduct;
    }

    @Override
    public String getEmailFromToken(String token) {
        return authService.getEmailFromToken(token);
    }

    @Override
    public List<PurchaseInvoiceDTO> getPurchasesByUserEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return List.of();
        List<Purchase> purchases = purchaseRepository.findByUser_Id(user.getId());
        List<PurchaseInvoiceDTO> invoices = new java.util.ArrayList<>();
        if (purchases == null) return invoices;
        for (Purchase purchase : purchases) {
            if (purchase.getStatus() != Purchase.Status.CONFIRMED) continue;
            PurchaseInvoiceDTO invoice = new PurchaseInvoiceDTO();
            invoice.setPurchaseId(purchase.getId());
            invoice.setPurchaseDate(purchase.getDate());
            invoice.setTotalAmount(purchase.getCart().getFinalPrice());
            List<ProductDetailDTO> products = new java.util.ArrayList<>();
            for (CartItem item : purchase.getCart().getItems()) {
                Product product = productRepository.findById(item.getId()).orElse(null);
                if (product == null) continue;
                ProductDetailDTO pd = new ProductDetailDTO();
                pd.setId(product.getId());
                pd.setDescription(product.getDescription());
                pd.setStock(product.getStock());
                pd.setPrice(product.getPrice());
                products.add(pd);
            }
            invoice.setProducts(products);
            invoices.add(invoice);
        }
        return invoices;
    }
}
