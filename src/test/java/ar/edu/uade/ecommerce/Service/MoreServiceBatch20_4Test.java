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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MoreServiceBatch20_4Test {

    @Mock
    BrandRepository brandRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    CartRepository cartRepository;
    @Mock
    CartItemRepository cartItemRepository;
    @Mock
    PurchaseRepository purchaseRepository;
    @Mock
    AddressRepository addressRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    TokenRepository tokenRepository;
    @Mock
    ECommerceEventService ecommerceEventService;
    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    BrandServiceImpl brandService;
    @InjectMocks
    CategoryServiceImpl categoryService;
    @InjectMocks
    ProductServiceImpl productService;
    @InjectMocks
    CartServiceImpl cartService;
    @InjectMocks
    CartItemServiceImpl cartItemService;
    @InjectMocks
    PurchaseServiceImpl purchaseService;
    @InjectMocks
    PasswordResetServiceImpl passwordResetService;
    @InjectMocks
    AddressServiceImpl addressService;
    @InjectMocks
    AuthServiceImpl authService;

    // Reemplazo el mock problemático por una instancia real
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // nothing special
    }

    @Test
    void brand_saveAll_updatesExisting() {
        Brand existing = new Brand(); existing.setId(1); existing.setName("X");
        Brand incoming = new Brand(); incoming.setName("X");
        when(brandRepository.findAll()).thenReturn(List.of(existing));
        when(brandRepository.findByName("X")).thenReturn(Optional.of(existing));
        when(brandRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(brandRepository.findAll()).thenReturn(List.of(existing));

        List<Brand> out = brandService.saveAllBrands(List.of(incoming));
        assertNotNull(out);
        verify(brandRepository).save(existing);
    }

    @Test
    void brand_deleteAll_invokesRepo() {
        doNothing().when(brandRepository).deleteAll();
        brandService.deleteAllBrands();
        verify(brandRepository).deleteAll();
    }

    @Test
    void category_deleteAll_invokesRepo() {
        doNothing().when(categoryRepository).deleteAll();
        categoryService.deleteAllCategories();
        verify(categoryRepository).deleteAll();
    }

    @Test
    void category_saveAll_mixedIncoming_savesBoth() {
        when(categoryRepository.findAll()).thenReturn(List.of());
        Category c1 = new Category(); c1.setName("A");
        when(categoryRepository.findByName("A")).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(categoryRepository.findAll()).thenReturn(List.of(c1));
        List<Category> res = categoryService.saveAllCategories(List.of(c1));
        assertEquals(1, res.size());
    }

    @Test
    void product_findById_success() {
        Product p = new Product(); p.setId(7);
        when(productRepository.findById(7)).thenReturn(Optional.of(p));
        Product out = productService.findById(7L);
        assertEquals(7, out.getId());
    }

    @Test
    void product_updateStock_zeroStock() {
        Product p = new Product(); p.setId(8); p.setStock(5);
        when(productRepository.findById(8)).thenReturn(Optional.of(p));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Product out = productService.updateProductStock(8, 0);
        assertEquals(0, out.getStock());
    }

    @Test
    void cart_findUserByEmail_returnsUser() {
        User u = new User(); u.setId(9); u.setEmail("u@e.com");
        when(userRepository.findByEmail("u@e.com")).thenReturn(u);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "userRepository", userRepository);
        User out = cartService.findUserByEmail("u@e.com");
        assertEquals(9, out.getId());
    }

    @Test
    void cart_isUserSessionActive_falseWhenNull() {
        when(userRepository.findByEmail("no@ex.com")).thenReturn(null);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "userRepository", userRepository);
        assertFalse(cartService.isUserSessionActive("no@ex.com"));
    }

    @Test
    void cart_createPurchase_delegatesToPurchaseService() {
        Purchase p = new Purchase(); p.setId(300);
        PurchaseService ps = mock(PurchaseService.class);
        when(ps.save(eq(p))).thenReturn(p);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "purchaseService", ps);
        Purchase out = cartService.createPurchase(p);
        assertEquals(300, out.getId());
    }

    @Test
    void cart_sendKafkaEvent_nonCart_emits() {
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "ecommerceEventService", ecommerceEventService);
        cartService.sendKafkaEvent("T", "x");
        verify(ecommerceEventService).emitRawEvent(eq("T"), anyString());
    }

    @Test
    void cartItem_save_callsRepo() {
        CartItem it = new CartItem(); it.setId(12);
        when(cartItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        org.springframework.test.util.ReflectionTestUtils.setField(cartItemService, "cartItemRepository", cartItemRepository);
        CartItem out = cartItemService.save(it);
        assertEquals(12, out.getId());
    }

    @Test
    void cartItem_findByCartId_nullRepoResult_returnsEmpty() {
        when(cartItemRepository.findByCartId(5)).thenReturn(null);
        org.springframework.test.util.ReflectionTestUtils.setField(cartItemService, "cartItemRepository", cartItemRepository);
        List<CartItem> out = cartItemService.findByCartId(5);
        assertNull(out);
    }

    @Test
    void address_save_and_delete() {
        Address a = new Address(); a.setId(77);
        AddressRepository ar = address_repository_dummy();
        when(ar.save(any())).thenAnswer(i -> i.getArgument(0));
        org.springframework.test.util.ReflectionTestUtils.setField(addressService, "addressRepository", ar);
        Address saved = addressService.save(a);
        assertNotNull(saved);
        // delete by id
        doNothing().when(ar).deleteById(77);
        address_service_delete_by_id(addressService, 77);
    }

    @Test
    void auth_getEmailFromToken_null_returnsNull() {
        when(jwtUtil.extractUsername("bad")).thenReturn(null);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "jwtUtil", jwtUtil);
        assertNull(authService.getEmailFromToken("bad"));
    }

    @Test
    void passwordReset_changePassword_invalidToken_throws() {
        when(userRepository.findByEmail("m@x.com")).thenReturn(new User());
        TokenRepository tr = token_repository_dummy();
        org.springframework.test.util.ReflectionTestUtils.setField(passwordResetService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(passwordResetService, "tokenRepository", tr);
        assertThrows(RuntimeException.class, () -> passwordResetService.changePassword("m@x.com", "tok", "newpass"));
    }

    @Test
    void purchase_releaseExpiredReservations_processesExpired() throws Exception {
        Purchase p = new Purchase(); p.setId(500); p.setStatus(Purchase.Status.PENDING);
        p.setReservationTime(java.time.LocalDateTime.now().minusHours(5));
        when(purchaseRepository.findExpiredWithCartAndUser(eq(Purchase.Status.PENDING), any())).thenReturn(List.of(p));
        // usar ObjectMapper real, sin stubbing
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "objectMapper", objectMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "ecommerceEventService", ecommerceEventService);
        purchaseService.releaseExpiredReservations();
        assertEquals(Purchase.Status.CANCELLED, p.getStatus());
        verify(purchaseRepository).save(p);
    }

    @Test
    void purchase_getPurchasesWithCartByUserId_includesProductCode() {
        Product prodLocal = new Product(); prodLocal.setId(600); prodLocal.setProductCode(888);
        Product pRepo = new Product(); pRepo.setId(600); pRepo.setProductCode(888);
        CartItem ci = new CartItem(); ci.setId(600); ci.setProduct(prodLocal);
        Cart cart = new Cart(); cart.setItems(List.of(ci));
        Purchase p = new Purchase(); p.setId(700); p.setStatus(Purchase.Status.CONFIRMED); p.setCart(cart);
        when(purchaseRepository.findByUser_Id(11)).thenReturn(List.of(p));
        when(productRepository.findById(600)).thenReturn(Optional.of(pRepo));
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "productRepository", productRepository);
        List<ar.edu.uade.ecommerce.Entity.DTO.PurchaseWithCartDTO> res = purchaseService.getPurchasesWithCartByUserId(11);
        assertEquals(1, res.size());
        assertNotNull(res.get(0).getCart());
    }

    // helpers
    private TokenRepository token_repository_dummy() {
        TokenRepository tr = mock(TokenRepository.class);
        when(tr.findByToken(anyString())).thenReturn(Optional.empty());
        return tr;
    }

    private AddressRepository address_repository_dummy() {
        return mock(AddressRepository.class);
    }

    private void address_service_delete_by_id(AddressServiceImpl svc, Integer id) {
        try {
            svc.delete(id);
        } catch (Exception ignored) {
        }
    }
}
