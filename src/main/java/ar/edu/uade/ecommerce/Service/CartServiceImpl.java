package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.CartRepository;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ar.edu.uade.ecommerce.Repository.ProductRepository productRepository;


    @Autowired
    private ar.edu.uade.ecommerce.Service.PurchaseService purchaseService;

    @Autowired
    private ar.edu.uade.ecommerce.messaging.ECommerceEventService ecommerceEventService;

    @Override
    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Cart findById(Integer id) {
        return cartRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        cartRepository.deleteById(id);
    }

    @Override
    public List<Cart> findAll() {
        return cartRepository.findAll();
    }

    @Override
    public Cart createCart(Cart cart) {
        float finalPrice = 0f;
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                Product productRequest = item.getProduct();
                if (productRequest == null || productRequest.getId() == null) {
                    throw new IllegalArgumentException("Producto inválido en el carrito");
                }
                Product product = productRepository.findById(productRequest.getId()).orElse(null);
                if (product == null) {
                    throw new IllegalArgumentException("Producto no encontrado: " + productRequest.getId());
                }
                if (item.getQuantity() == null || item.getQuantity() <= 0 || product.getStock() < item.getQuantity()) {
                    throw new IllegalArgumentException("No hay suficiente stock para el producto: " + product.getId());
                }
                item.setProduct(product);
                if (product.getPrice() != null && item.getQuantity() != null) {
                    finalPrice += product.getPrice() * item.getQuantity();
                }
            }
        }
        cart.setFinalPrice(finalPrice);
        Cart created = cartRepository.save(cart);
        try {
            // Emitir únicamente el evento de compra pendiente al crear el carrito
            sendKafkaEvent("POST: Compra pendiente", created);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CartServiceImpl.class).error("Error enviando evento de compra pendiente al crear carrito", e);
        }
        return created;
    }

    @Override
    public Cart updateCart(Integer id, Cart cart) {
        Cart existing = cartRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setFinalPrice(cart.getFinalPrice());
            existing.setUser(cart.getUser());
            return cartRepository.save(existing);
        }
        return null;
    }

    @Override
    public void deleteCart(Integer id) {
        cartRepository.deleteById(id);
    }

    @Override
    public String getEmailFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }

    @Override
    public boolean isUserSessionActive(String email) {
        User user = userRepository.findByEmail(email);
        return user != null && Boolean.TRUE.equals(user.getSessionActive());
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public ar.edu.uade.ecommerce.Entity.Purchase createPurchase(ar.edu.uade.ecommerce.Entity.Purchase purchase) {
        // Simplemente delega la creación al PurchaseService para evitar duplicación de lógica
        return purchaseService.save(purchase);
    }

    // Helper: revisar carritos expirados; la tarea programada central está en PurchaseServiceImpl
    public void releaseExpiredCarts() {
        List<Cart> carts = cartRepository.findAll();
        for (Cart cart : carts) {
            // Buscar compras pendientes asociadas a este carrito (consulta específica para evitar traer todo)
            List<Purchase> purchases = purchaseRepository.findByCart_IdAndStatus(cart.getId(), Purchase.Status.PENDING);
            for (Purchase purchase : purchases) {
                if (purchase.getReservationTime().plusHours(4).isBefore(LocalDateTime.now())) {
                    // Lanzar evento por la API de Comunicación para rollback de stock
                    try {
                        // Usar el builder seguro de payload para evitar serializar entidades JPA completas
                        sendKafkaEvent("CartExpired_RollbackStock", cart);
                    } catch (Exception e) {
                        org.slf4j.LoggerFactory.getLogger(CartServiceImpl.class).error("Error enviando evento de rollback de stock", e);
                    }
                    // Cancelar la compra
                    purchase.setStatus(Purchase.Status.CANCELLED);
                    purchaseRepository.save(purchase);
                }
            }
        }
    }

    @Override
    public void updateProductStock(Product product) {
        productRepository.save(product);
    }

    @Override
    public void sendKafkaEvent(String eventName, Object payload) {
        try {
            // Si el payload es un Cart, armar el evento completo
            if (payload instanceof Cart cart) {
                // Buscar la compra asociada al carrito
                ar.edu.uade.ecommerce.Entity.Purchase purchase = null;
                if (cart.getId() != null && purchaseRepository != null) {
                    purchase = purchaseRepository.findByCartId(cart.getId());
                }
                // Armar usuario
                java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                if (cart.getUser() != null) {
                    userMap.put("name", cart.getUser().getName());
                    userMap.put("email", cart.getUser().getEmail());
                }
                // Armar items
                java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
                if (cart.getItems() != null) {
                    for (CartItem item : cart.getItems()) {
                        Product product = item.getProduct();
                        if (product != null) {
                            java.util.Map<String, Object> prodDetail = new java.util.HashMap<>();
                            // Usar productCode en lugar de id
                            prodDetail.put("productCode", product.getProductCode());
                            prodDetail.put("title", product.getTitle());
                            prodDetail.put("quantity", item.getQuantity());
                            prodDetail.put("price", product.getPrice());
                            items.add(prodDetail);
                        }
                    }
                }
                // Armar cart
                java.util.Map<String, Object> cartMap = new java.util.HashMap<>();
                cartMap.put("items", items);
                cartMap.put("finalPrice", cart.getFinalPrice());

                // Armar evento final
                java.util.Map<String, Object> eventDetail = new java.util.HashMap<>();
                // Ajuste: para rollback, el type interno debe ser "POST: Stock rollback - compra cancelada"
                String innerType = eventName;
                if ("StockRollback_CartCancelled".equals(eventName)) {
                    innerType = "POST: Stock rollback - compra cancelada";
                }

                // Forzar status según tipo de evento
                String statusValue;
                if ("POST: Compra pendiente".equals(eventName)) {
                    statusValue = "PENDING";
                } else if ("StockRollback_CartCancelled".equals(eventName) || "POST: Stock rollback - compra cancelada".equals(innerType)) {
                    statusValue = "CANCELLED";
                } else {
                    statusValue = (purchase != null ? String.valueOf(purchase.getStatus()) : null);
                }

                // Armar payload
                java.util.Map<String, Object> payloadMap = new java.util.HashMap<>();
                payloadMap.put("purchaseId", purchase != null ? purchase.getId() : null);
                payloadMap.put("user", userMap);
                payloadMap.put("cart", cartMap);
                payloadMap.put("status", statusValue);

                eventDetail.put("type", innerType);
                eventDetail.put("payload", payloadMap);
                eventDetail.put("timestamp", java.time.ZonedDateTime.now().toString());
                String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(eventDetail);
                System.out.println("Evento enviado por Kafka: " + json); // Imprime el JSON por pantalla
                ecommerceEventService.emitRawEvent(eventName, json);
                return;
            }
            // Si el payload no es Cart, enviar como antes
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            ecommerceEventService.emitRawEvent(eventName, json);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CartServiceImpl.class).error("Error enviando evento mockeado por Kafka", e);
        }
    }

    // Nuevo helper: arma y envía el evento a partir de la Purchase ya cargada (evita consultas extra)
    private void sendKafkaEventForPurchase(String eventName, Purchase purchase) {
        if (purchase == null) return;
        Cart cart = purchase.getCart();
        java.util.Map<String, Object> userMap = new java.util.HashMap<>();
        if (purchase.getUser() != null) {
            userMap.put("name", purchase.getUser().getName());
            userMap.put("email", purchase.getUser().getEmail());
        } else if (cart != null && cart.getUser() != null) {
            userMap.put("name", cart.getUser().getName());
            userMap.put("email", cart.getUser().getEmail());
        }

        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        if (cart != null && cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    java.util.Map<String, Object> prodDetail = new java.util.HashMap<>();
                    // Usar productCode en lugar de id
                    prodDetail.put("productCode", product.getProductCode());
                    prodDetail.put("title", product.getTitle());
                    prodDetail.put("quantity", item.getQuantity());
                    prodDetail.put("price", product.getPrice());
                    items.add(prodDetail);
                }
            }
        }

        java.util.Map<String, Object> cartMap = new java.util.HashMap<>();
        if (cart != null) {
            cartMap.put("items", items);
            cartMap.put("finalPrice", cart.getFinalPrice());
        }

        java.util.Map<String, Object> payloadMap = new java.util.HashMap<>();
        payloadMap.put("purchaseId", purchase.getId());
        payloadMap.put("user", userMap);
        payloadMap.put("cart", cartMap);
        payloadMap.put("status", purchase.getStatus());

        try {
            java.util.Map<String, Object> eventDetail = new java.util.HashMap<>();
            eventDetail.put("type", eventName);
            eventDetail.put("payload", payloadMap);
            eventDetail.put("timestamp", java.time.ZonedDateTime.now().toString());
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(eventDetail);
            ecommerceEventService.emitRawEvent(eventName, json);
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(CartServiceImpl.class).error("Error serializando evento para purchase", ex);
        }
    }

    @Override
    public void revertProductStock(Cart cart) {
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();
                if (product != null && item.getQuantity() != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }
        // Enviar mensaje detallado por la API de Comunicación
        sendKafkaEvent("StockRollback_CartCancelled", cart);
    }

    @Override
    public void confirmProductStock(Cart cart) {
        // No emitir evento extra (StockConfirmed_CartPurchase). El único evento a emitir será
        // "POST: Compra confirmada" desde el controlador/servicio centralizado.
        // Mantener este método por compatibilidad sin efectos colaterales.
    }

    @Override
    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }
}
