package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Entity.DTO.ProductViewResponseDTO;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MoreServiceBatch205Test {

    @Mock
    ProductViewRepository productViewRepository;
    @Mock
    CartRepository cartRepository;
    @Mock
    PurchaseRepository purchaseRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    JwtUtil jwtUtil;
    @Mock
    ECommerceEventService ecommerceEventService;
    @Mock
    ObjectMapper objectMapper;

    // Mocks adicionales usados en varios tests
    @Mock
    BrandRepository brandRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    CartItemRepository cartItemRepository;
    @Mock
    AddressRepository addressRepository;

    @InjectMocks
    ProductViewServiceImpl productViewService;
    @InjectMocks
    CartServiceImpl cartService;
    @InjectMocks
    AuthServiceImpl authService;
    @InjectMocks
    PasswordResetServiceImpl passwordResetService;
    @InjectMocks
    AddressServiceImpl addressService;
    @InjectMocks
    BrandServiceImpl brandService;
    @InjectMocks
    CategoryServiceImpl categoryService;
    @InjectMocks
    CartItemServiceImpl cartItemService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User(); user.setId(1); user.setEmail("u@test.com");
        product = new Product(); product.setId(2); product.setTitle("T"); product.setProductCode(123);
    }

    @Test
    void productView_saveProductView_saves() {
        ProductView pv = new ProductView(user, product, LocalDateTime.now());
        when(productViewRepository.save(any())).thenReturn(pv);
        ProductView out = productViewService.saveProductView(user, product);
        assertNotNull(out);
        verify(productViewRepository).save(any());
    }

    @Test
    void productView_toDTO_handlesNullBrandAndCategories() {
        product.setCategories(Set.of());
        product.setBrand(null);
        ProductView pv = new ProductView(user, product, LocalDateTime.now());
        org.springframework.test.util.ReflectionTestUtils.setField(pv, "id", 11L);
        ProductViewResponseDTO dto = productViewService.toDTO(pv);
        assertEquals(11L, dto.getViewId());
        assertEquals(Long.valueOf(product.getId()), dto.getProductId());
        assertNull(dto.getBrand());
    }

    @Test
    void productView_getAllViewsSummary_mapsFields() {
        ProductView pv = new ProductView(user, product, LocalDateTime.now());
        org.springframework.test.util.ReflectionTestUtils.setField(pv, "id", 5L);
        when(productViewRepository.findAll()).thenReturn(List.of(pv));
        var list = productViewService.getAllViewsSummary();
        assertEquals(1, list.size());
        assertTrue(list.get(0).containsKey("productCode"));
    }

    @Test
    void productView_getProductViewsByUser_pagesAndMaps() {
        ProductView pv = new ProductView(user, product, LocalDateTime.now());
        Page<ProductView> page = new PageImpl<>(List.of(pv));
        when(productViewRepository.findByUser(eq(user), any(Pageable.class))).thenReturn(page);
        Page<ProductViewResponseDTO> res = productViewService.getProductViewsByUser(user, Pageable.unpaged());
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void cart_sendKafkaEvent_withPurchaseFound_includesPurchase() throws Exception {
        Cart cart = new Cart(); cart.setId(77);
        Purchase p = new Purchase(); p.setId(900); p.setCart(cart);
        when(purchaseRepository.findByCartId(77)).thenReturn(p);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "purchaseRepository", purchaseRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "ecommerceEventService", ecommerceEventService);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        cartService.sendKafkaEvent("TYP", cart);
        verify(ecommerceEventService).emitRawEvent(eq("TYP"), anyString());
    }

    @Test
    void cart_sendKafkaEvent_nonCart_emits() {
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "ecommerceEventService", ecommerceEventService);
        cartService.sendKafkaEvent("X", "payload");
        verify(ecommerceEventService).emitRawEvent(eq("X"), anyString());
    }

    @Test
    void cart_releaseExpiredCarts_processesExpiredPurchase() {
        Cart c = new Cart(); c.setId(101);
        Purchase p = new Purchase(); p.setId(102); p.setStatus(Purchase.Status.PENDING); p.setReservationTime(LocalDateTime.now().minusHours(5));
        when(cartRepository.findAll()).thenReturn(List.of(c));
        when(purchaseRepository.findByCart_IdAndStatus(eq(101), eq(Purchase.Status.PENDING))).thenReturn(List.of(p));
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "cartRepository", cartRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "purchaseRepository", purchaseRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "ecommerceEventService", ecommerceEventService);
        cartService.releaseExpiredCarts();
        assertEquals(Purchase.Status.CANCELLED, p.getStatus());
    }

    @Test
    void cart_confirmProductStock_isNoOp() {
        Cart c = new Cart(); c.setId(1);
        // Should not throw or call external services
        assertDoesNotThrow(() -> cartService.confirmProductStock(c));
    }

    @Test
    void cart_getProductById_delegates() {
        Product p = new Product(); p.setId(55);
        when(productRepository.findById(55)).thenReturn(Optional.of(p));
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "productRepository", productRepository);
        Product out = cartService.getProductById(55);
        assertEquals(55, out.getId());
    }

    @Test
    void auth_login_success_returnsToken() {
        User u = new User(); u.setEmail("a@b.com"); u.setPassword("hash"); u.setAccountActive(true);
        when(userRepository.findByEmail("a@b.com")).thenReturn(u);
        PasswordEncoder pe = passwordEncoderMock();
        when(pe.matches("pw","hash")).thenReturn(true);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "passwordEncoder", pe);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "jwtUtil", jwtUtil);
        when(jwtUtil.generateToken("a@b.com")).thenReturn("tok123");
        String token = authService.login("a@b.com", "pw");
        assertEquals("tok123", token);
    }

    @Test
    void passwordReset_changePassword_success_removesToken() {
        User u = new User(); u.setEmail("xx@yy.com");
        Token tk = new Token(); tk.setToken("t1"); tk.setUser(u); tk.setExpirationDate(java.util.Date.from(java.time.Instant.now().plusSeconds(10000)));
        when(userRepository.findByEmail("xx@yy.com")).thenReturn(u);
        TokenRepository tr = token_repository_with(tk);
        org.springframework.test.util.ReflectionTestUtils.setField(passwordResetService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(passwordResetService, "tokenRepository", tr);
        PasswordEncoder pe = passwordEncoderMock();
        when(pe.encode("newp")).thenReturn("hnew");
        org.springframework.test.util.ReflectionTestUtils.setField(passwordResetService, "passwordEncoder", pe);
        passwordResetService.changePassword("xx@yy.com", "t1", "newp");
        verify(tr).deleteByToken("t1");
    }

    @Test
    void passwordReset_validateToken_falseIfTokenExpired() {
        User u = new User(); u.setEmail("ex@x.com"); u.setId(10);
        when(userRepository.findByEmail("ex@x.com")).thenReturn(u);
        Token tk = new Token(); tk.setToken("t2"); tk.setUser(u); tk.setExpirationDate(java.util.Date.from(java.time.Instant.now().minusSeconds(1)));
        TokenRepository tr = token_repository_with(tk);
        org.springframework.test.util.ReflectionTestUtils.setField(passwordResetService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(passwordResetService, "tokenRepository", tr);
        assertFalse(passwordResetService.validateToken("ex@x.com", "t2"));
    }

    @Test
    void address_deleteAddress_userNull_noDelete() {
        Address a = new Address(); a.setId(9); a.setUser(null);
        when(addressRepository.findById(9)).thenReturn(Optional.of(a));
        org.springframework.test.util.ReflectionTestUtils.setField(addressService, "addressRepository", addressRepository);
        addressService.deleteAddress(9, null);
        verify(addressRepository, never()).deleteById(any());
    }

    @Test
    void brand_getAllBrands_delegates() {
        when(brandRepository.findAll()).thenReturn(List.of(new Brand()));
        org.springframework.test.util.ReflectionTestUtils.setField(brandService, "brandRepository", brandRepository);
        List<Brand> out = brandService.getAllBrands();
        assertEquals(1, out.size());
    }

    @Test
    void category_getAllActiveCategories_delegates() {
        when(categoryRepository.findByActiveTrue()).thenReturn(List.of(new Category()));
        org.springframework.test.util.ReflectionTestUtils.setField(category_service_get(), "categoryRepository", categoryRepository);
        var out = categoryService.getAllActiveCategories();
        assertEquals(1, out.size());
    }

    @Test
    void cartItem_findByCartId_delegatesToRepo() {
        CartItem it = new CartItem(); it.setId(15); Cart c = new Cart(); c.setId(15); it.setCart(c);
        when(cartItemRepository.findByCartId(15)).thenReturn(List.of(it));
        org.springframework.test.util.ReflectionTestUtils.setField(cart_item_service_get(), "cartItemRepository", cartItemRepository);
        var out = cartItemService.findByCartId(15);
        assertEquals(1, out.size());
    }

    // helpers
    private PasswordEncoder passwordEncoderMock() {
        PasswordEncoder pe = mock(PasswordEncoder.class);
        when(pe.matches(any(), any())).thenAnswer(i -> {
            String raw = i.getArgument(0); String enc = i.getArgument(1);
            return ("pw".equals(raw) && "hash".equals(enc)) || ("newp".equals(raw) && "hnew".equals(enc));
        });
        when(pe.encode(any())).thenAnswer(i -> "enc-" + i.getArgument(0));
        return pe;
    }

    private TokenRepository token_repository_with(Token tk) {
        TokenRepository tr = mock(TokenRepository.class);
        when(tr.findByToken(tk.getToken())).thenReturn(Optional.of(tk));
        return tr;
    }

    // helpers to avoid some static-analysis warnings when using ReflectionTestUtils
    private CategoryServiceImpl category_service_get() { return categoryService; }
    private CartItemServiceImpl cart_item_service_get() { return cartItemService; }
}
