package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.CartRepository;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
    private ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService kafkaMockService;

    @Autowired
    private ar.edu.uade.ecommerce.Service.PurchaseService purchaseService;

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
        // Verificar stock de cada producto antes de crear el carrito
        float finalPrice = 0f;
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                Product productRequest = item.getProduct();
                if (productRequest == null || productRequest.getId() == null) {
                    throw new IllegalArgumentException("Producto inválido en el carrito");
                }
                // Buscar el producto real en la base de datos
                Product product = productRepository.findById(productRequest.getId()).orElse(null);
                if (product == null) {
                    throw new IllegalArgumentException("Producto no encontrado: " + productRequest.getId());
                }
                if (product.getStock() < item.getQuantity()) {
                    throw new IllegalArgumentException("No hay suficiente stock para el producto: " + product.getId());
                }
                // Asociar el producto real al CartItem
                item.setProduct(product);
                // Calcular el precio final sumando el precio del producto por la cantidad requerida
                if (product.getPrice() != null && item.getQuantity() != null) {
                    finalPrice += product.getPrice() * item.getQuantity();
                }
            }
        }
        cart.setFinalPrice(finalPrice);
        Cart created = cartRepository.save(cart);
        // Lanzar evento por Kafka al crear el carrito para reserva de stock
        try {
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(created);
            kafkaMockService.sendEvent(new ar.edu.uade.ecommerce.Entity.Event("CartCreated_ReserveStock", json));
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CartServiceImpl.class).error("Error enviando evento de reserva de stock", e);
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

    @Scheduled(fixedRate = 60000) // Cada 1 minuto
    public void releaseExpiredCarts() {
        List<Cart> carts = cartRepository.findAll();
        for (Cart cart : carts) {
            // Buscar compras pendientes asociadas a este carrito
            List<Purchase> purchases = purchaseRepository.findAll().stream()
                .filter(p -> p.getCart().getId().equals(cart.getId()) && p.getStatus() == Purchase.Status.PENDING && p.getReservationTime() != null)
                .toList();
            for (Purchase purchase : purchases) {
                if (purchase.getReservationTime().plusHours(4).isBefore(LocalDateTime.now())) {
                    // Lanzar evento por Kafka para rollback de stock
                    try {
                        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(cart);
                        kafkaMockService.sendEvent(new ar.edu.uade.ecommerce.Entity.Event("CartExpired_RollbackStock", json));
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
            // Si el payload es un Cart, armar el detalle de productos
            if (payload instanceof Cart cart) {
                java.util.List<java.util.Map<String, Object>> products = new java.util.ArrayList<>();
                if (cart.getItems() != null) {
                    for (CartItem item : cart.getItems()) {
                        Product product = item.getProduct();
                        if (product != null) {
                            java.util.Map<String, Object> prodDetail = new java.util.HashMap<>();
                            prodDetail.put("productId", product.getId());
                            prodDetail.put("title", product.getTitle());
                            prodDetail.put("stockAffected", item.getQuantity());
                            prodDetail.put("stockAfter", product.getStock());
                            products.add(prodDetail);
                        }
                    }
                }
                java.util.Map<String, Object> eventDetail = new java.util.HashMap<>();
                eventDetail.put("event", eventName);
                eventDetail.put("cartId", cart.getId());
                eventDetail.put("products", products);
                String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(eventDetail);
                kafkaMockService.sendEvent(new ar.edu.uade.ecommerce.Entity.Event(eventName, json));
                return;
            }
            // Si el payload no es Cart, enviar como antes
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            kafkaMockService.sendEvent(new ar.edu.uade.ecommerce.Entity.Event(eventName, json));
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CartServiceImpl.class).error("Error enviando evento mockeado por Kafka", e);
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
        // Enviar mensaje detallado por Kafka
        sendKafkaEvent("StockRollback_CartCancelled", cart);
    }

    @Override
    public void confirmProductStock(Cart cart) {
        // Solo enviar el mensaje detallado por Kafka, no modificar el stock en la tabla product
        sendKafkaEvent("StockConfirmed_CartPurchase", cart);
    }

    @Override
    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }
}
