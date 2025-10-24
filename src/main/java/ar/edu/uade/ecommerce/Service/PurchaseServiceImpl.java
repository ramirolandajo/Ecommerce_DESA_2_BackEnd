package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Entity.Purchase.Status;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
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
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;

@Service
public class PurchaseServiceImpl implements PurchaseService {
    @Autowired
    private PurchaseRepository purchaseRepository;

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

    @Autowired
    private ECommerceEventService ecommerceEventService;

    private static final Logger logger = LoggerFactory.getLogger(PurchaseServiceImpl.class);

    @Override
    public Purchase save(Purchase purchase) {
        // Si la compra pasa a PENDING, setear reservationTime
        if (purchase.getStatus() == Status.PENDING && purchase.getReservationTime() == null) {
            purchase.setReservationTime(LocalDateTime.now());
            // Avisar por la API de Comunicación que se debe reservar el stock
            try {
                String json = objectMapper.writeValueAsString(purchase);
                Event event = new Event("ReserveStock", json);
                ecommerceEventService.emitRawEvent(event.getType(), json);
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
        Purchase purchase = findById(id);
        if (purchase != null && purchase.getStatus() != Status.CANCELLED) {
            purchase.setStatus(Status.CANCELLED);
            purchaseRepository.save(purchase);
            // Eliminado: no emitir aquí evento de rollback para evitar duplicado.
            // La emisión única se realiza desde CartServiceImpl.revertProductStock (invocado en el controlador).
        }
    }

    @Override
    public Purchase confirmPurchase(Integer id) {
        Purchase purchase = findById(id);
        if (purchase == null) {
            logger.warn("Intento de confirmar compra inexistente. ID: {}", id);
            return null;
        }
        logger.info("Confirmando compra. Estado previo: {}", purchase.getStatus());
        purchase.setStatus(Status.CONFIRMED);
        purchase.setDate(LocalDateTime.now()); // Setea la fecha de confirmación
        purchaseRepository.save(purchase);
        // Eliminado: la emisión del evento "POST: Compra confirmada" se realiza desde el controlador.
        logger.info("Compra confirmada. Estado actual: {}", purchase.getStatus());
        return purchase;
    }

    @Override
    public void addProductToCart(Integer cartId, Integer productId, int quantity) {
        // Lógica para agregar producto al carrito
        // ...
        Event event = new Event("ProductAddedToCart", "CartId: " + cartId + ", ProductId: " + productId + ", Quantity: " + quantity);
        ecommerceEventService.emitRawEvent(event.getType(), event.getPayload());
    }

    @Override
    public void editCartItem(Integer cartItemId, int newQuantity) {
        // Lógica para editar cantidad de producto en el carrito
        // ...
        Event event = new Event("CartItemEdited", "CartItemId: " + cartItemId + ", NewQuantity: " + newQuantity);
        ecommerceEventService.emitRawEvent(event.getType(), event.getPayload());
    }

    @Override
    public void removeProductFromCart(Integer cartItemId) {
        // Lógica para eliminar producto del carrito
        // ...
        Event event = new Event("ProductRemovedFromCart", "CartItemId: " + cartItemId);
        ecommerceEventService.emitRawEvent(event.getType(), event.getPayload());
    }

    @Scheduled(fixedRate = 60000) // Cada 1 minuto
    @org.springframework.transaction.annotation.Transactional
    public void releaseExpiredReservations() {
        // Buscar compras PENDING cuya reservationTime sea anterior a ahora-4h
        java.time.LocalDateTime cutoff = LocalDateTime.now().minusHours(4);
        // Usar la consulta con JOIN FETCH para evitar consultas adicionales al acceder a cart/user
        List<Purchase> expired = purchaseRepository.findExpiredWithCartAndUser(Status.PENDING, cutoff);
        for (Purchase purchase : expired) {
            try {
                // Armar un payload seguro a partir de la purchase cargada (cart + user ya están fetch)
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("purchaseId", purchase.getId());
                payload.put("cartId", purchase.getCart() != null ? purchase.getCart().getId() : null);
                if (purchase.getCart() != null && purchase.getCart().getUser() != null) {
                    java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("id", purchase.getCart().getUser().getId());
                    userMap.put("email", purchase.getCart().getUser().getEmail());
                    payload.put("user", userMap);
                }
                String json = objectMapper.writeValueAsString(payload);
                ecommerceEventService.emitRawEvent("ReleaseStock", json);
            } catch (Exception e) {
                logger.error("Error al liberar reserva", e);
            }
            purchase.setStatus(Status.CANCELLED);
            purchaseRepository.save(purchase);
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
            if (purchase.getCart() == null) {
                invoice.setTotalAmount(0.0F);
                invoice.setProducts(new java.util.ArrayList<>());
                invoices.add(invoice);
                continue;
            }
            invoice.setTotalAmount(purchase.getCart().getFinalPrice());
            List<ProductDetailDTO> products = new java.util.ArrayList<>();
            List<CartItem> items = purchase.getCart().getItems();
            if (items != null) {
                for (CartItem item : items) {
                    Product product = productRepository.findById(item.getId()).orElse(null);
                    if (product == null) continue;
                    ProductDetailDTO pd = new ProductDetailDTO();
                    pd.setId(product.getId());
                    pd.setDescription(product.getDescription());
                    pd.setStock(product.getStock());
                    pd.setPrice(product.getPrice());
                    products.add(pd);
                }
            }
            invoice.setProducts(products);
            invoices.add(invoice);
        }
        return invoices;
    }

    @Override
    public Purchase findLastPendingPurchaseByUserWithinHours(Integer userId, int hours) {
        List<Purchase> purchases = purchaseRepository.findByUser_IdAndStatusOrderByReservationTimeDesc(userId, Purchase.Status.PENDING);
        if (purchases == null) return null;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (Purchase purchase : purchases) {
            if (purchase.getReservationTime() != null && purchase.getReservationTime().plusHours(hours).isAfter(now)) {
                return purchase;
            }
        }
        return null;
    }

    @Override
    public List<Purchase> findByUserId(Integer id) {
        return purchaseRepository.findByUser_Id(id);
    }

    @Override
    public List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO> getPurchasesWithCartByUserId(Integer userId) {
        List<Purchase> purchases = purchaseRepository.findByUser_Id(userId);
        List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO> dtos = new java.util.ArrayList<>();
        for (Purchase purchase : purchases) {
            if (purchase.getStatus() != Purchase.Status.CONFIRMED) continue;
            ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO dto = new ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO();
            dto.setId(purchase.getId());
            dto.setDate(purchase.getDate());
            dto.setReservationTime(purchase.getReservationTime());
            dto.setDirection(purchase.getDirection());
            dto.setStatus(purchase.getStatus() != null ? purchase.getStatus().name() : null);
            if (purchase.getCart() != null) {
                ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO.CartDTO cartDto = new ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO.CartDTO();
                cartDto.setId(purchase.getCart().getId());
                cartDto.setFinalPrice(purchase.getCart().getFinalPrice());
                java.util.List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO.CartItemDTO> itemDtos = new java.util.ArrayList<>();
                if (purchase.getCart().getItems() != null) {
                    for (ar.edu.uade.ecommerce.Entity.CartItem item : purchase.getCart().getItems()) {
                        ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO.CartItemDTO itemDto = new ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO.CartItemDTO();
                        itemDto.setId(item.getId());
                        itemDto.setQuantity(item.getQuantity());
                        if (item.getProduct() != null) {
                            ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO.ProductDTO productDto = new ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO.ProductDTO();
                            productDto.setId(item.getProduct().getId());
                            productDto.setTitle(item.getProduct().getTitle());
                            productDto.setDescription(item.getProduct().getDescription());
                            productDto.setPrice(item.getProduct().getPrice());
                            productDto.setMediaSrc(item.getProduct().getMediaSrc());
                            // Nuevo: agregar productCode también aquí
                            productDto.setProductCode(item.getProduct().getProductCode());
                            itemDto.setProduct(productDto);
                        }
                        itemDtos.add(itemDto);
                    }
                }
                cartDto.setItems(itemDtos);
                dto.setCart(cartDto);
            }
            dtos.add(dto);
        }
        return dtos;
    }
}
