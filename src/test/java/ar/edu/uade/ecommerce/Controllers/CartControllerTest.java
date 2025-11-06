package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Cart;
import ar.edu.uade.ecommerce.Entity.CartItem;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartControllerTest {
    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCartById_Success() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findById(1)).thenReturn(cart);
        ResponseEntity<Cart> response = cartController.getCartById(token, 1);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(cart, response.getBody());
    }

    @Test
    void testGetCartById_Unauthorized() {
        String token = "Bearer testtoken";
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(false);
        ResponseEntity<Cart> response = cartController.getCartById(token, 1);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testGetCartById_NotFound() {
        String token = "Bearer testtoken";
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findById(1)).thenReturn(null);
        ResponseEntity<Cart> response = cartController.getCartById(token, 1);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCreateCart_Success() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        User user = new User();
        user.setSessionActive(true);
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_Unauthorized() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(false);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testDeleteCart_Success() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findById(1)).thenReturn(cart);
        doNothing().when(cartService).deleteCart(1);
        ResponseEntity<Void> response = cartController.deleteCart(token, 1);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteCart_Unauthorized() {
        String token = "Bearer testtoken";
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(false);
        ResponseEntity<Void> response = cartController.deleteCart(token, 1);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testDeleteCart_NotFound() {
        String token = "Bearer testtoken";
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findById(1)).thenReturn(null);
        ResponseEntity<Void> response = cartController.deleteCart(token, 1);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCreateCart_NullUser() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(null);
        Cart createdCart = new Cart();
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_NullItems() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        cart.setItems(null);
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_ItemWithNullProduct() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item = new CartItem();
        item.setProduct(null);
        item.setQuantity(2);
        cart.setItems(List.of(item));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_ItemWithNullQuantity() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product = new ar.edu.uade.ecommerce.Entity.Product();
        product.setPrice(100f);
        item.setProduct(product);
        item.setQuantity(null);
        cart.setItems(List.of(item));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_ItemWithNullQuantity_CreatedCartHasSameItem() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product = new ar.edu.uade.ecommerce.Entity.Product();
        product.setId(123);
        product.setPrice(100f);
        item.setProduct(product);
        item.setQuantity(null); // quantity es null
        cart.setItems(List.of(item));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        createdCart.setItems(List.of(item)); // el mismo item con quantity null
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
        assertNull(response.getHeaders().getFirst("X-Stock-Error"));
        assertNull(response.getHeaders().getFirst("X-Kafka-Error"));
    }

    @Test
    void testCreateCart_ItemWithNullPrice() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product = new ar.edu.uade.ecommerce.Entity.Product();
        product.setPrice(null);
        item.setProduct(product);
        item.setQuantity(2);
        cart.setItems(List.of(item));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_CreateCartReturnsNull() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        when(cartService.createCart(any(Cart.class))).thenReturn(null);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_ProductByIdReturnsNull() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product = new ar.edu.uade.ecommerce.Entity.Product();
        product.setId(123);
        product.setPrice(100f);
        item.setProduct(product);
        item.setQuantity(2);
        cart.setItems(List.of(item));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        createdCart.setItems(List.of(item));
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        when(cartService.getProductById(123)).thenReturn(null);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_UpdateProductStockThrowsException() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product = new ar.edu.uade.ecommerce.Entity.Product();
        product.setId(123);
        product.setStock(10);
        product.setPrice(100f);
        item.setProduct(product);
        item.setQuantity(2);
        cart.setItems(List.of(item));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        createdCart.setItems(List.of(item));
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Product realProduct = new ar.edu.uade.ecommerce.Entity.Product();
        realProduct.setId(123);
        realProduct.setStock(10);
        when(cartService.getProductById(123)).thenReturn(realProduct);
        doThrow(new RuntimeException("Stock error")).when(cartService).updateProductStock(any());
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_SendKafkaEventThrowsException() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        doThrow(new RuntimeException("Kafka error")).when(cartService).sendKafkaEvent(anyString(), any());
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_CreatePurchaseReturnsNull() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        when(cartService.createPurchase(any())).thenReturn(null);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testCreateCart_StockErrorHeader() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product = new ar.edu.uade.ecommerce.Entity.Product();
        product.setId(123);
        product.setStock(10);
        product.setPrice(100f);
        item.setProduct(product);
        item.setQuantity(2);
        cart.setItems(List.of(item));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        createdCart.setItems(List.of(item));
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Product realProduct = new ar.edu.uade.ecommerce.Entity.Product();
        realProduct.setId(123);
        realProduct.setStock(10);
        when(cartService.getProductById(123)).thenReturn(realProduct);
        doThrow(new RuntimeException("Stock error")).when(cartService).updateProductStock(any());
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
        String stockHeader = response.getHeaders().getFirst("X-Stock-Error");
        String kafkaHeader = response.getHeaders().getFirst("X-Kafka-Error");
        assertEquals("true", stockHeader != null ? stockHeader : "false");
        assertEquals("false", kafkaHeader != null ? kafkaHeader : "false");
    }

    @Test
    void testCreateCart_KafkaErrorHeader() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);

        Cart createdCart = new Cart();
        CartItem createdItem = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product = new ar.edu.uade.ecommerce.Entity.Product();
        product.setId(123);
        product.setPrice(100f);
        product.setStock(10);
        createdItem.setProduct(product);
        createdItem.setQuantity(1);
        createdCart.setItems(List.of(createdItem));

        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        when(cartService.getProductById(123)).thenReturn(product);
        doThrow(new RuntimeException("Kafka error")).when(cartService).sendKafkaEvent(anyString(), any());
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);

        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
        String stockHeader = response.getHeaders().getFirst("X-Stock-Error");
        String kafkaHeader = response.getHeaders().getFirst("X-Kafka-Error");
        assertEquals("false", stockHeader != null ? stockHeader : "false");
        assertEquals("false", kafkaHeader != null ? kafkaHeader : "false");
    }

    @Test
    void testCreateCart_StockAndKafkaErrorHeader() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product = new ar.edu.uade.ecommerce.Entity.Product();
        product.setId(123);
        product.setStock(10);
        product.setPrice(100f);
        item.setProduct(product);
        item.setQuantity(2);
        cart.setItems(List.of(item));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        createdCart.setItems(List.of(item));
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Product realProduct = new ar.edu.uade.ecommerce.Entity.Product();
        realProduct.setId(123);
        realProduct.setStock(10);
        when(cartService.getProductById(123)).thenReturn(realProduct);
        doThrow(new RuntimeException("Stock error")).when(cartService).updateProductStock(any());
        doThrow(new RuntimeException("Kafka error")).when(cartService).sendKafkaEvent(anyString(), any());
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
    }

    @Test
    void testCreateCart_NoStockOrKafkaErrorHeader() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product = new ar.edu.uade.ecommerce.Entity.Product();
        product.setId(123);
        product.setStock(10);
        product.setPrice(100f);
        item.setProduct(product);
        item.setQuantity(2);
        cart.setItems(List.of(item));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        createdCart.setItems(List.of(item));
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Product realProduct = new ar.edu.uade.ecommerce.Entity.Product();
        realProduct.setId(123);
        realProduct.setStock(10);
        when(cartService.getProductById(123)).thenReturn(realProduct);
        // No lanzamos excepción en updateProductStock ni sendKafkaEvent
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
        assertNull(response.getHeaders().getFirst("X-Stock-Error"));
        assertNull(response.getHeaders().getFirst("X-Kafka-Error"));
    }

    @Test
    void testCreateCart_CreatedCartItemsNull() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        createdCart.setItems(null); // items es null
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
        assertNull(response.getHeaders().getFirst("X-Stock-Error"));
        assertNull(response.getHeaders().getFirst("X-Kafka-Error"));
    }

    @Test
    void testCreateCart_ProductAndQuantityNotNull_CoversBranch() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product = new ar.edu.uade.ecommerce.Entity.Product();
        product.setId(123);
        product.setStock(10);
        product.setPrice(100f);
        item.setProduct(product);
        item.setQuantity(2); // quantity no es null
        cart.setItems(List.of(item));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        createdCart.setItems(List.of(item)); // el mismo item con product y quantity no null
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Product realProduct = new ar.edu.uade.ecommerce.Entity.Product();
        realProduct.setId(123);
        realProduct.setStock(10);
        when(cartService.getProductById(123)).thenReturn(realProduct);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
        assertNull(response.getHeaders().getFirst("X-Stock-Error"));
        assertNull(response.getHeaders().getFirst("X-Kafka-Error"));
    }

    @Test
    void testCreateCart_MultipleItems_CoversAllBranches() {
        String token = "Bearer testtoken";
        Cart cart = new Cart();
        CartItem item1 = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product1 = new ar.edu.uade.ecommerce.Entity.Product();
        product1.setId(123);
        product1.setStock(10);
        product1.setPrice(100f);
        item1.setProduct(product1);
        item1.setQuantity(2); // ambos no null

        CartItem item2 = new CartItem();
        item2.setProduct(null); // product null
        item2.setQuantity(5);

        CartItem item3 = new CartItem();
        ar.edu.uade.ecommerce.Entity.Product product3 = new ar.edu.uade.ecommerce.Entity.Product();
        product3.setId(456);
        product3.setStock(5);
        product3.setPrice(50f);
        item3.setProduct(product3);
        item3.setQuantity(null); // quantity null

        cart.setItems(List.of(item1, item2, item3));
        User user = new User();
        when(cartService.getEmailFromToken("testtoken")).thenReturn("test@email.com");
        when(cartService.isUserSessionActive("test@email.com")).thenReturn(true);
        when(cartService.findUserByEmail("test@email.com")).thenReturn(user);
        Cart createdCart = new Cart();
        createdCart.setItems(List.of(item1, item2, item3));
        when(cartService.createCart(any(Cart.class))).thenReturn(createdCart);
        ar.edu.uade.ecommerce.Entity.Product realProduct1 = new ar.edu.uade.ecommerce.Entity.Product();
        realProduct1.setId(123);
        realProduct1.setStock(10);
        when(cartService.getProductById(123)).thenReturn(realProduct1);
        ar.edu.uade.ecommerce.Entity.Product realProduct3 = new ar.edu.uade.ecommerce.Entity.Product();
        realProduct3.setId(456);
        realProduct3.setStock(5);
        when(cartService.getProductById(456)).thenReturn(realProduct3);
        ar.edu.uade.ecommerce.Entity.Purchase purchase = new ar.edu.uade.ecommerce.Entity.Purchase();
        when(cartService.createPurchase(any())).thenReturn(purchase);
        ResponseEntity<ar.edu.uade.ecommerce.Entity.Purchase> response = cartController.createCart(token, cart);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(purchase, response.getBody());
        assertNull(response.getHeaders().getFirst("X-Stock-Error"));
        assertNull(response.getHeaders().getFirst("X-Kafka-Error"));
    }
}
