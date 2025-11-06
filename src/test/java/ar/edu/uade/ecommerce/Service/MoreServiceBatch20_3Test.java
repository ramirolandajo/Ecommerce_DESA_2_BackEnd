package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.*;
import ar.edu.uade.ecommerce.Security.JwtUtil;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoreServiceBatch20_3Test {

    @Mock
    BrandRepository brandRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    CartRepository cartRepository;
    @Mock
    AddressRepository addressRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    PurchaseRepository purchaseRepository;
    @Mock
    TokenRepository tokenRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtUtil jwtUtil;
    @Mock
    ECommerceEventService ecommerceEventService;
    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    BrandServiceImpl brandService = new BrandServiceImpl();
    @InjectMocks
    CategoryServiceImpl categoryService = new CategoryServiceImpl();
    @InjectMocks
    ProductServiceImpl productService = new ProductServiceImpl();
    @InjectMocks
    CartServiceImpl cartService = new CartServiceImpl();
    @InjectMocks
    PurchaseServiceImpl purchaseService = new PurchaseServiceImpl();
    @InjectMocks
    AuthServiceImpl authService = new AuthServiceImpl();
    @InjectMocks
    PasswordResetServiceImpl passwordResetService = new PasswordResetServiceImpl();
    @InjectMocks
    AddressServiceImpl addressService = new AddressServiceImpl();

    private Product prod;
    private User user;

    @BeforeEach
    void setUp() {
        prod = new Product(); prod.setId(11); prod.setStock(4); prod.setPrice(null);
        user = new User(); user.setId(7); user.setEmail("u@ex.com");
    }

    @Test
    void brand_getAllActiveBrands_dtoHasKeys() {
        Brand b = new Brand(); b.setId(3); b.setName("Z"); b.setBrandCode(42); b.setActive(true);
        when(brandRepository.findByActiveTrue()).thenReturn(List.of(b));
        java.util.Collection<?> res = brandService.getAllActiveBrands();
        assertEquals(1, res.size());
        Object first = res.iterator().next();
        assertTrue(first instanceof java.util.Map);
        java.util.Map<?,?> map = (java.util.Map<?,?>) first;
        assertTrue(map.containsKey("id") && map.containsKey("name") && map.containsKey("brandCode"));
    }

    @Test
    void category_saveCategory_withIdAndInactive_keepsInactive() {
        Category c = new Category(); c.setId(5); c.setName("C"); c.setActive(false);
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Category out = categoryService.saveCategory(c);
        assertFalse(out.isActive());
    }

    @Test
    void createCart_priceNull_resultsFinalPriceZero() {
        Product p = new Product(); p.setId(50); p.setPrice(null); p.setStock(10);
        CartItem it = new CartItem(); it.setProduct(p); it.setQuantity(2);
        Cart cart = new Cart(); cart.setItems(List.of(it));
        when(productRepository.findById(50)).thenReturn(Optional.of(p));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        java.util.Collection<?> saved = null;
        // inject productRepository and cartRepository
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "productRepository", productRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "cartRepository", cartRepository);
        Cart out = cartService.createCart(cart);
        assertEquals(0.0f, out.getFinalPrice());
    }

    @Test
    void createCart_nullQuantity_throws() {
        Product p = new Product(); p.setId(51); p.setPrice(2.0f); p.setStock(10);
        CartItem it = new CartItem(); it.setProduct(p); it.setQuantity(null);
        Cart cart = new Cart(); cart.setItems(List.of(it));
        when(productRepository.findById(51)).thenReturn(Optional.of(p));
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "productRepository", productRepository);
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(cart));
    }

    @Test
    void sendKafkaEvent_nonCart_payloadDelegated() throws Exception {
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "ecommerceEventService", ecommerceEventService);
        cartService.sendKafkaEvent("TYP", "simplePayload");
        verify(ecommerceEventService).emitRawEvent(eq("TYP"), anyString());
    }

    @Test
    void sendKafkaEvent_cartWithNullProductCode_stillEmits() throws Exception {
        Product p = new Product(); p.setId(60); p.setProductCode(null); p.setPrice(10.0f);
        CartItem it = new CartItem(); it.setProduct(p); it.setQuantity(1);
        Cart cart = new Cart(); cart.setId(70); cart.setItems(List.of(it));
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "purchaseRepository", purchaseRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "ecommerceEventService", ecommerceEventService);
        cartService.sendKafkaEvent("EV", cart);
        verify(ecommerceEventService).emitRawEvent(eq("EV"), anyString());
    }

    @Test
    void revertProductStock_quantityNull_doesNotSave() {
        Product p = new Product(); p.setId(80); p.setStock(1);
        CartItem it = new CartItem(); it.setProduct(p); it.setQuantity(null);
        Cart cart = new Cart(); cart.setItems(List.of(it));
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "productRepository", productRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "ecommerceEventService", ecommerceEventService);
        cartService.revertProductStock(cart);
        verify(productRepository, never()).save(any());
    }

    @Test
    void purchase_getPurchasesByUserEmail_mapsProductsSkippingMissing() {
        User u = new User(); u.setId(100); u.setEmail("x@y.com");
        Cart cart = new Cart(); cart.setFinalPrice(50.0f);
        CartItem ci = new CartItem(); ci.setId(5); cart.setItems(List.of(ci));
        Purchase p = new Purchase(); p.setId(200); p.setStatus(Purchase.Status.CONFIRMED); p.setCart(cart);
        when(userRepository.findByEmail("x@y.com")).thenReturn(u);
        when(purchaseRepository.findByUser_Id(u.getId())).thenReturn(List.of(p));
        // productRepository will return empty, so products list in invoice should be empty
        when(productRepository.findById(5)).thenReturn(Optional.empty());
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "productRepository", productRepository);
        List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO> res = purchaseService.getPurchasesByUserEmail("x@y.com");
        assertEquals(1, res.size());
        assertEquals(0, res.get(0).getProducts().size());
    }

    @Test
    void mockStockChange_delegatesToProductService() {
        ProductService prodSvc = mock(ProductService.class);
        Product updated = new Product(); updated.setId(300);
        when(prodSvc.updateProductStock(300, 99)).thenReturn(updated);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "productService", prodSvc);
        String msg = purchaseService.mockStockChange(300, 99);
        assertTrue(msg.contains("Producto actualizado") || msg.contains("300"));
    }


    @Test
    void auth_login_incorrectPassword_throws() {
        User u = new User(); u.setEmail("a@b.com"); u.setPassword("hashed"); u.setAccountActive(true);
        when(userRepository.findByEmail("a@b.com")).thenReturn(u);
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);
        assertThrows(RuntimeException.class, () -> authService.login("a@b.com", "wrong"));
    }

    @Test
    void auth_verifyEmailToken_success_activatesUser() {
        User u = new User(); u.setId(55); u.setEmail("t@t.com");
        Token tk = new Token(); tk.setToken("tt"); tk.setUser(u); tk.setExpirationDate(java.util.Date.from(java.time.Instant.now().plusSeconds(3600)));
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "tokenRepository", tokenRepository);
        when(userRepository.findByEmail("t@t.com")).thenReturn(u);
        when(tokenRepository.findByToken("tt")).thenReturn(java.util.Optional.of(tk));
        when(userRepository.save(u)).thenReturn(u);
        boolean ok = authService.verifyEmailToken("t@t.com", "tt");
        assertTrue(ok);
        verify(tokenRepository).deleteByUserId(55);
    }

    @Test
    void address_getAddressesByUser_delegatesToRepo() {
        User u = new User(); u.setId(77);
        Address a = new Address(); a.setId(88); a.setUser(u);
        when(addressRepository.findByUser(u)).thenReturn(List.of(a));
        org.springframework.test.util.ReflectionTestUtils.setField(addressService, "addressRepository", addressRepository);
        List<Address> out = addressService.getAddressesByUser(u);
        assertEquals(1, out.size());
    }

    @Test
    void cart_basicRepositoryMethods_findAll_findById_delete() {
        when(cartRepository.findAll()).thenReturn(List.of(new Cart()));
        when(cartRepository.findById(5)).thenReturn(Optional.of(new Cart()));
        doNothing().when(cartRepository).deleteById(10);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "cartRepository", cartRepository);
        assertEquals(1, cartService.findAll().size());
        assertNotNull(cartService.findById(5));
        assertDoesNotThrow(() -> cartService.delete(10));
    }

    // helpers/mocks for tests
    private TokenRepository token_repository_dummy() {
        TokenRepository tr = mock(TokenRepository.class);
        when(tr.findByToken(anyString())).thenReturn(Optional.empty());
        return tr;
    }

    private TokenRepository token_repository_for_tests() {
        return token_repository_dummy();
    }

    private AuthServiceImpl auth_service_for_tests() {
        return authService;
    }
}
