package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO;
import ar.edu.uade.ecommerce.Entity.Purchase;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AuthService;
import ar.edu.uade.ecommerce.Service.PurchaseService;
import ar.edu.uade.ecommerce.Service.UserService;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ar.edu.uade.ecommerce.Service.CartService cartService;

    @Autowired
    private ar.edu.uade.ecommerce.Service.AddressService addressService;

    @Autowired
    private ECommerceEventService ecommerceEventService;

    @GetMapping
    public List<Purchase> getAllPurchases() {
        return purchaseService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO> getPurchaseById(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        Purchase purchase = purchaseService.findById(id);
        if (purchase == null) {
            // Según los tests actuales, debe devolver 200 con body null cuando no existe la compra
            return ResponseEntity.ok(null);
        }
        ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO dto = mapPurchaseToDTO(purchase);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Purchase> createPurchase(@RequestHeader("Authorization") String authHeader, @RequestBody Purchase purchase) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        // Setear el usuario y el estado PENDING automáticamente
        purchase.setUser(user);
        purchase.setStatus(Purchase.Status.PENDING);
        Purchase created = purchaseService.save(purchase);

        // Ya no emitimos aquí "POST: Compra pendiente" para evitar duplicados.
        // La emisión se realiza únicamente al crear el carrito en CartServiceImpl.createCart().

        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}") //cancelo la compra por tiempo de vida de la compra (4 horas) o manualmente se cancela
    public ResponseEntity<?> deletePurchase(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        Purchase purchase = purchaseService.findById(id);
        if (purchase != null && purchase.getCart() != null) {
            // Realizar solamente el rollback de inventario (no emitir evento de "Compra cancelada")
            cartService.revertProductStock(purchase.getCart());
        }

        purchaseService.deleteById(id);
        return ResponseEntity.ok("Compra eliminada correctamente. ID: " + id);
    }

    @PostMapping("/{id}/confirm/{addressId}") //confirmo la compra y genero la factura
    public ResponseEntity<Purchase> confirmPurchase(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id, @PathVariable(required = false) Integer addressId) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        Purchase purchase = purchaseService.confirmPurchase(id);
        if (purchase == null) {
            // Según los tests deben recibir 200 con body null cuando la compra no existe
            return ResponseEntity.ok(null);
        }
        // Si no se pasó addressId, no intentar buscar la dirección
        if (addressId != null) {
            Address address = addressService.findById(addressId);
            if (address == null) {
                return ResponseEntity.badRequest().body(null);
            }
            purchase.setDirection(address.getDescription());
        }
        purchaseService.save(purchase);
        if (purchase.getCart() != null) {
            // Confirmar el stock y enviar evento por Kafka
            cartService.confirmProductStock(purchase.getCart());
        }

        // Emitir evento compra confirmada
        try {
            Map<String, Object> userMap = mapUser(purchase.getUser());
            Map<String, Object> cartMap = mapCart(purchase.getCart());
            ecommerceEventService.emitPurchaseConfirmed(purchase.getId(), userMap, cartMap);
        } catch (Exception ex) {
            System.err.println("Error emitiendo evento de compra confirmada: " + ex.getMessage());
        }

        return ResponseEntity.ok(purchase);
    }

    @GetMapping("/my-purchases")
    public ResponseEntity<List<PurchaseInvoiceDTO>> getMyPurchases(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        List<PurchaseInvoiceDTO> invoices = purchaseService.getPurchasesByUserEmail(email);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/pending-cart")
    public ResponseEntity<Purchase> getPendingCart(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        // Buscar la última compra pendiente del usuario cuyo tiempo de vida sea menor a 4 horas
        Purchase pending = purchaseService.findLastPendingPurchaseByUserWithinHours(user.getId(), 4);
        if (pending == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/user-purchases")
    public ResponseEntity<List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO>> getPurchasesByUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = purchaseService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        if (user == null || !user.getSessionActive()) {
            return ResponseEntity.status(401).build();
        }
        List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO> purchases = purchaseService.getPurchasesWithCartByUserId(user.getId());
        return ResponseEntity.ok(purchases);
    }

    private ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO mapPurchaseToDTO(Purchase purchase) {
        if (purchase == null) return null;
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
                        // Nuevo: agregar productCode en el DTO de respuesta
                        productDto.setProductCode(item.getProduct().getProductCode());
                        itemDto.setProduct(productDto);
                    }
                    itemDtos.add(itemDto);
                }
            }
            cartDto.setItems(itemDtos);
            dto.setCart(cartDto);
        }
        return dto;
    }

    private Map<String, Object> mapUser(User user) {
        Map<String, Object> m = new HashMap<>();
        if (user == null) return m;
        m.put("id", user.getId() != null ? Long.valueOf(user.getId()) : null);
        m.put("name", user.getName());
        m.put("email", user.getEmail());
        return m;
    }

    private Map<String, Object> mapCart(ar.edu.uade.ecommerce.Entity.Cart cart) {
        Map<String, Object> m = new HashMap<>();
        if (cart == null) return m;
        m.put("cartId", cart.getId() != null ? Long.valueOf(cart.getId()) : null);
        m.put("finalPrice", cart.getFinalPrice());
        java.util.List<Map<String, Object>> items = new java.util.ArrayList<>();
        if (cart.getItems() != null) {
            for (ar.edu.uade.ecommerce.Entity.CartItem ci : cart.getItems()) {
                Map<String, Object> it = new HashMap<>();
                it.put("id", ci.getId() != null ? Long.valueOf(ci.getId()) : null);
                if (ci.getProduct() != null) {
                    it.put("productCode", ci.getProduct().getProductCode());
                    it.put("title", ci.getProduct().getTitle());
                    it.put("price", ci.getProduct().getPrice());
                } else {
                    it.put("productCode", null);
                }
                it.put("quantity", ci.getQuantity());
                items.add(it);
            }
        }
        // Unificar clave de items como 'items'
        m.put("items", items);
        return m;
    }
}
