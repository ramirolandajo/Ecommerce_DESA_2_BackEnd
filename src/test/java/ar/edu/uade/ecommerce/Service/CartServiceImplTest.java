package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.CartRepository;
import ar.edu.uade.ecommerce.Repository.PurchaseRepository;
import ar.edu.uade.ecommerce.Repository.UserRepository;
import ar.edu.uade.ecommerce.Security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.LocalDateTime;

public class CartServiceImplTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private ar.edu.uade.ecommerce.Repository.ProductRepository productRepository;
    @Mock
    private ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService kafkaMockService;
    @Mock
    private ar.edu.uade.ecommerce.Service.PurchaseService purchaseService;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCart_success() {
        Product product = new Product();
        product.setId(1);
        product.setStock(10);
        product.setPrice(100f);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        List<CartItem> items = List.of(item);
        Cart cart = new Cart();
        cart.setItems(items);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        Cart result = cartService.createCart(cart);
        assertEquals(200f, result.getFinalPrice());
        verify(kafkaMockService, times(1)).sendEvent(any());
    }

    @Test
    void testCreateCart_insufficientStock() {
        Product product = new Product();
        product.setId(1);
        product.setStock(1);
        product.setPrice(100f);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
    }

    @Test
    void testCreateCart_stockEqualsQuantity() {
        Product product = new Product();
        product.setId(1);
        product.setStock(2);
        product.setPrice(50f);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        Cart result = cartService.createCart(cart);
        assertEquals(100f, result.getFinalPrice());
    }

    @Test
    void testCreateCart_zeroPriceOrQuantity() {
        Product product = new Product();
        product.setId(1);
        product.setStock(10);
        product.setPrice(0f);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(0); // cantidad cero, debe lanzar excepción
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
    }

    @Test
    void testCreateCart_emptyItems() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        Cart result = cartService.createCart(cart);
        assertEquals(0f, result.getFinalPrice());
    }

    @Test
    void testCreateCart_saveException() {
        Product product = new Product();
        product.setId(1);
        product.setStock(10);
        product.setPrice(100f);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> cartService.createCart(cart));
    }

    @Test
    void testSendKafkaEvent_withCart() throws Exception {
        Product product = new Product();
        product.setId(1);
        product.setTitle("Test");
        product.setStock(5);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        Cart cart = new Cart();
        cart.setId(99);
        cart.setItems(List.of(item));
        cartService.sendKafkaEvent("TestEvent", cart);
        verify(kafkaMockService, times(1)).sendEvent(any());
    }

    @Test
    void testSendKafkaEvent_payloadNotCart() {
        String eventName = "TestEvent";
        String payload = "simple string payload";
        cartService.sendKafkaEvent(eventName, payload);
        verify(kafkaMockService, times(1)).sendEvent(any());
    }

    @Test
    void testSendKafkaEvent_kafkaException() {
        Cart cart = new Cart();
        cart.setId(1);
        doThrow(new RuntimeException("Kafka error")).when(kafkaMockService).sendEvent(any());
        cartService.sendKafkaEvent("TestEvent", cart);
        // No exception debe propagarse
    }

    @Test
    void testRevertProductStock() {
        Product product = new Product();
        product.setId(1);
        product.setStock(5);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        cartService.revertProductStock(cart);
        assertEquals(7, product.getStock());
        verify(productRepository, times(1)).save(product);
        verify(kafkaMockService, times(1)).sendEvent(any());
    }

    @Test
    void testRevertProductStock_nullItems() {
        Cart cart = new Cart();
        cart.setItems(null);
        cartService.revertProductStock(cart);
        verify(kafkaMockService, times(1)).sendEvent(any());
    }

    @Test
    void testRevertProductStock_nullProductOrQuantity() {
        CartItem item1 = new CartItem();
        item1.setProduct(null);
        item1.setQuantity(1);
        CartItem item2 = new CartItem();
        Product product = new Product();
        product.setId(1);
        product.setStock(5);
        item2.setProduct(product);
        item2.setQuantity(null);
        Cart cart = new Cart();
        cart.setItems(List.of(item1, item2));
        cartService.revertProductStock(cart);
        verify(kafkaMockService, times(1)).sendEvent(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testIsUserSessionActive_true() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setSessionActive(true);
        when(userRepository.findByEmail("test@test.com")).thenReturn(user);
        assertTrue(cartService.isUserSessionActive("test@test.com"));
    }

    @Test
    void testIsUserSessionActive_false() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(null);
        assertFalse(cartService.isUserSessionActive("test@test.com"));
    }

    @Test
    void testIsUserSessionActive_nullSessionActive() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setSessionActive(null);
        when(userRepository.findByEmail("test@test.com")).thenReturn(user);
        assertFalse(cartService.isUserSessionActive("test@test.com"));
    }

    @Test
    void testUpdateCart_found() {
        Cart existing = new Cart();
        existing.setId(1);
        Cart update = new Cart();
        update.setFinalPrice(123f);
        update.setUser(new User());
        when(cartRepository.findById(1)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any(Cart.class))).thenReturn(existing);
        Cart result = cartService.updateCart(1, update);
        assertEquals(123f, result.getFinalPrice());
    }

    @Test
    void testUpdateCart_notFound() {
        when(cartRepository.findById(1)).thenReturn(Optional.empty());
        Cart update = new Cart();
        assertNull(cartService.updateCart(1, update));
    }

    @Test
    void testFindById_found() {
        Cart cart = new Cart();
        cart.setId(1);
        when(cartRepository.findById(1)).thenReturn(Optional.of(cart));
        Cart result = cartService.findById(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testFindById_notFound() {
        when(cartRepository.findById(2)).thenReturn(Optional.empty());
        Cart result = cartService.findById(2);
        assertNull(result);
    }

    @Test
    void testGetProductById_found() {
        Product product = new Product();
        product.setId(1);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        Product result = cartService.getProductById(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testGetProductById_notFound() {
        when(productRepository.findById(2)).thenReturn(Optional.empty());
        Product result = cartService.getProductById(2);
        assertNull(result);
    }

    @Test
    void testSave() {
        Cart cart = new Cart();
        when(cartRepository.save(cart)).thenReturn(cart);
        Cart result = cartService.save(cart);
        assertEquals(cart, result);
    }

    @Test
    void testUpdateProductStock() {
        Product product = new Product();
        when(productRepository.save(product)).thenReturn(product);
        cartService.updateProductStock(product);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testDelete() {
        cartService.delete(1);
        verify(cartRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteCart() {
        cartService.deleteCart(2);
        verify(cartRepository, times(1)).deleteById(2);
    }

    @Test
    void testGetEmailFromToken() {
        when(jwtUtil.extractUsername("token123")).thenReturn("user@email.com");
        String email = cartService.getEmailFromToken("token123");
        assertEquals("user@email.com", email);
    }

    @Test
    void testFindUserByEmail() {
        User user = new User();
        user.setEmail("user@email.com");
        when(userRepository.findByEmail("user@email.com")).thenReturn(user);
        User result = cartService.findUserByEmail("user@email.com");
        assertEquals(user, result);
    }

    @Test
    void testCreatePurchase() {
        Purchase purchase = new Purchase();
        when(purchaseService.save(purchase)).thenReturn(purchase);
        Purchase result = cartService.createPurchase(purchase);
        assertEquals(purchase, result);
    }

    @Test
    void testConfirmProductStock() {
        Cart cart = new Cart();
        cartService.confirmProductStock(cart);
        verify(kafkaMockService, times(1)).sendEvent(any());
    }

    @Test
    void testFindAll() {
        List<Cart> carts = List.of(new Cart(), new Cart());
        when(cartRepository.findAll()).thenReturn(carts);
        List<Cart> result = cartService.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testReleaseExpiredCarts_expiredPurchase() {
        Cart cart = new Cart();
        cart.setId(1);
        Purchase purchase = new Purchase();
        purchase.setCart(cart);
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        List<Cart> carts = List.of(cart);
        List<Purchase> purchases = List.of(purchase);
        when(cartRepository.findAll()).thenReturn(carts);
        when(purchaseRepository.findAll()).thenReturn(purchases);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        cartService.releaseExpiredCarts();
        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
        verify(kafkaMockService, atLeastOnce()).sendEvent(any());
        verify(purchaseRepository, times(1)).save(purchase);
    }

    @Test
    void testReleaseExpiredCarts_noExpiredPurchase() {
        Cart cart = new Cart();
        cart.setId(1);
        Purchase purchase = new Purchase();
        purchase.setCart(cart);
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now());
        List<Cart> carts = List.of(cart);
        List<Purchase> purchases = List.of(purchase);
        when(cartRepository.findAll()).thenReturn(carts);
        when(purchaseRepository.findAll()).thenReturn(purchases);
        cartService.releaseExpiredCarts();
        assertEquals(Purchase.Status.PENDING, purchase.getStatus());
        verify(kafkaMockService, never()).sendEvent(any());
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void testReleaseExpiredCarts_statusNotPending() {
        Cart cart = new Cart();
        cart.setId(1);
        Purchase purchase = new Purchase();
        purchase.setCart(cart);
        purchase.setStatus(Purchase.Status.CANCELLED);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        List<Cart> carts = List.of(cart);
        List<Purchase> purchases = List.of(purchase);
        when(cartRepository.findAll()).thenReturn(carts);
        when(purchaseRepository.findAll()).thenReturn(purchases);
        cartService.releaseExpiredCarts();
        verify(kafkaMockService, never()).sendEvent(any());
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void testReleaseExpiredCarts_nullReservationTime() {
        Cart cart = new Cart();
        cart.setId(1);
        Purchase purchase = new Purchase();
        purchase.setCart(cart);
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(null);
        List<Cart> carts = List.of(cart);
        List<Purchase> purchases = List.of(purchase);
        when(cartRepository.findAll()).thenReturn(carts);
        when(purchaseRepository.findAll()).thenReturn(purchases);
        cartService.releaseExpiredCarts();
        verify(kafkaMockService, never()).sendEvent(any());
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void testReleaseExpiredCarts_noCarts() {
        when(cartRepository.findAll()).thenReturn(new ArrayList<>());
        cartService.releaseExpiredCarts();
        verify(purchaseRepository, never()).save(any());
        verify(kafkaMockService, never()).sendEvent(any());
    }

    @Test
    void testReleaseExpiredCarts_noPurchases() {
        Cart cart = new Cart();
        cart.setId(1);
        when(cartRepository.findAll()).thenReturn(List.of(cart));
        when(purchaseRepository.findAll()).thenReturn(new ArrayList<>());
        cartService.releaseExpiredCarts();
        verify(purchaseRepository, never()).save(any());
        verify(kafkaMockService, never()).sendEvent(any());
    }

    @Test
    void testReleaseExpiredCarts_purchaseNotAssociated() {
        Cart cart = new Cart();
        cart.setId(1);
        Cart otherCart = new Cart();
        otherCart.setId(2); // id diferente
        Purchase purchase = new Purchase();
        purchase.setCart(otherCart);
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        when(cartRepository.findAll()).thenReturn(List.of(cart));
        when(purchaseRepository.findAll()).thenReturn(List.of(purchase));
        cartService.releaseExpiredCarts();
        verify(purchaseRepository, never()).save(any());
        verify(kafkaMockService, never()).sendEvent(any());
    }

    @Test
    void testReleaseExpiredCarts_purchaseCartIsNull() {
        Cart cart = new Cart();
        cart.setId(1);
        Purchase purchase = new Purchase();
        purchase.setCart(null); // Cart es null
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        when(cartRepository.findAll()).thenReturn(List.of(cart));
        when(purchaseRepository.findAll()).thenReturn(List.of(purchase));
        cartService.releaseExpiredCarts();
        verify(purchaseRepository, never()).save(any());
        verify(kafkaMockService, never()).sendEvent(any());
    }

    @Test
    void testReleaseExpiredCarts_purchaseCartIdIsNull() {
        Cart cart = new Cart();
        cart.setId(1);
        Cart otherCart = new Cart();
        // No se setea el id, queda null
        Purchase purchase = new Purchase();
        purchase.setCart(otherCart);
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        when(cartRepository.findAll()).thenReturn(List.of(cart));
        when(purchaseRepository.findAll()).thenReturn(List.of(purchase));
        cartService.releaseExpiredCarts();
        verify(purchaseRepository, never()).save(any());
        verify(kafkaMockService, never()).sendEvent(any());
    }

    @Test
    void testReleaseExpiredCarts_cartIdIsNull() {
        Cart cart = new Cart();
        // No se setea el id, queda null
        Cart otherCart = new Cart();
        otherCart.setId(2);
        Purchase purchase = new Purchase();
        purchase.setCart(otherCart);
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        when(cartRepository.findAll()).thenReturn(List.of(cart));
        when(purchaseRepository.findAll()).thenReturn(List.of(purchase));
        cartService.releaseExpiredCarts();
        verify(purchaseRepository, never()).save(any());
        verify(kafkaMockService, never()).sendEvent(any());
    }

    @Test
    void testReleaseExpiredCarts_bothCartIdsNull() {
        Cart cart = new Cart(); // id null
        Cart purchaseCart = new Cart(); // id null
        Purchase purchase = new Purchase();
        purchase.setCart(purchaseCart);
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        when(cartRepository.findAll()).thenReturn(List.of(cart));
        when(purchaseRepository.findAll()).thenReturn(List.of(purchase));
        cartService.releaseExpiredCarts();
        verify(purchaseRepository, never()).save(any());
        verify(kafkaMockService, never()).sendEvent(any());
    }

    @Test
    void testCreateCart_invalidProductInCart() {
        CartItem item = new CartItem();
        item.setProduct(null); // Producto inválido
        item.setQuantity(1);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
    }

    @Test
    void testCreateCart_productNotFound() {
        Product product = new Product();
        product.setId(99);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
    }

    @Test
    void testCreateCart_kafkaException() {
        Product product = new Product();
        product.setId(1);
        product.setStock(10);
        product.setPrice(100f);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("Kafka error")).when(kafkaMockService).sendEvent(any());
        Cart result = cartService.createCart(cart);
        assertEquals(100f, result.getFinalPrice());
        // No debe lanzar excepción
    }

    @Test
    void testReleaseExpiredCarts_kafkaException() {
        Cart cart = new Cart();
        cart.setId(1);
        Purchase purchase = new Purchase();
        purchase.setCart(cart);
        purchase.setStatus(Purchase.Status.PENDING);
        purchase.setReservationTime(LocalDateTime.now().minusHours(5));
        List<Cart> carts = List.of(cart);
        List<Purchase> purchases = List.of(purchase);
        when(cartRepository.findAll()).thenReturn(carts);
        when(purchaseRepository.findAll()).thenReturn(purchases);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        doThrow(new RuntimeException("Kafka error")).when(kafkaMockService).sendEvent(any());
        cartService.releaseExpiredCarts();
        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
        // No debe lanzar excepción
    }

    @Test
    void testCreateCart_itemsNull() {
        Cart cart = new Cart();
        cart.setItems(null);
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        Cart result = cartService.createCart(cart);
        assertEquals(0f, result.getFinalPrice());
    }

    @Test
    void testCreateCart_productIdNull() {
        Product product = new Product();
        product.setId(null); // id null
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
    }

    @Test
    void testCreateCart_priceOrQuantityNull() {
        Product product = new Product();
        product.setId(1);
        product.setStock(10);
        product.setPrice(null); // price null
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(null); // quantity null
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
    }

    @Test
    void createCart_productPriceNull_quantityNotNull() {
        Product product = new Product();
        product.setId(1);
        product.setPrice(null); // price null
        product.setStock(10);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2); // quantity not null
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        Cart result = cartService.createCart(cart);
        assertEquals(0f, result.getFinalPrice());
    }

    @Test
    void createCart_productPriceNull_quantityNull() {
        Product product = new Product();
        product.setId(1);
        product.setPrice(null); // price null
        product.setStock(10);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(null); // quantity null
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
    }

    @Test
    void createCart_productPriceAndQuantityNotNull_butQuantityNegative() {
        Product product = new Product();
        product.setId(1);
        product.setPrice(100f); // price not null
        product.setStock(10);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(-1); // cantidad negativa, pero no null
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        // Debe lanzar excepción por stock insuficiente, pero pasa por el if
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
    }

    @Test
    void createCart_productPriceNegative_quantityNotNull() {
        Product product = new Product();
        product.setId(1);
        product.setPrice(-10f); // precio negativo
        product.setStock(10);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2); // cantidad válida
        Cart cart = new Cart();
        cart.setItems(List.of(item));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        Cart result = cartService.createCart(cart);
        assertEquals(-20f, result.getFinalPrice());
    }
}
