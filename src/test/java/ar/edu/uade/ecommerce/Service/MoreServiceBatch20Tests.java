package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Entity.DTO.PurchaseInvoiceDTO;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoreServiceBatch20Tests {

    // Common mocks used across tests (we'll inject into specific services via @InjectMocks fields below)
    @Mock
    BrandRepository brandRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    AddressRepository addressRepository;
    @Mock
    CartRepository cartRepository;
    @Mock
    PurchaseRepository purchaseRepository;
    @Mock
    TokenRepository tokenRepository;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    ECommerceEventService ecommerceEventService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    BrandServiceImpl brandService = new BrandServiceImpl();
    @InjectMocks
    CategoryServiceImpl categoryService = new CategoryServiceImpl();
    @InjectMocks
    ProductServiceImpl productService = new ProductServiceImpl();
    @InjectMocks
    UserServiceImpl userService = new UserServiceImpl();
    @InjectMocks
    CartServiceImpl cartService = new CartServiceImpl();
    @InjectMocks
    PurchaseServiceImpl purchaseService = new PurchaseServiceImpl();
    @InjectMocks
    PasswordResetServiceImpl passwordResetService = new PasswordResetServiceImpl();
    @InjectMocks
    AuthServiceImpl authService = new AuthServiceImpl();
    @InjectMocks
    AddressServiceImpl addressService = new AddressServiceImpl();

    private Product sampleProduct;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product(); sampleProduct.setId(1); sampleProduct.setStock(10); sampleProduct.setPrice(5.0f);
        sampleUser = new User(); sampleUser.setId(2); sampleUser.setEmail("u@example.com");
    }

    @Test
    void category_saveCategory_nullName_returnsNull() {
        Category c = new Category(); c.setName(null);
        assertNull(categoryService.saveCategory(c));
    }

    @Test
    void category_saveAll_removesAndSaves() {
        Category old = new Category(); old.setId(1); old.setName("X");
        when(categoryRepository.findAll()).thenReturn(List.of(old));
        Category incoming = new Category(); incoming.setName("Y");
        when(categoryRepository.findByName("Y")).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(categoryRepository.findAll()).thenReturn(List.of(old, incoming));

        List<Category> res = categoryService.saveAllCategories(List.of(incoming));
        assertNotNull(res);
        verify(categoryRepository).delete(old);
    }

    @Test
    void brand_saveBrand_setsActiveTrueWhenNull() {
        Brand b = new Brand(); b.setName("B1");
        when(brandRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Brand saved = brandService.saveBrand(b);
        assertTrue(saved.isActive());
    }

    @Test
    void product_updateStock_notFound_throws() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.updateProductStock(99, 5));
    }

    @Test
    void product_updateStock_success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Product out = productService.updateProductStock(1, 3);
        assertEquals(3, out.getStock());
    }

    @Test
    void user_addAddress_savesViaRepository() {
        Address a = new Address(); a.setUser(sampleUser); a.setDescription("home");
        when(addressRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Address out = userService.addAddress(a);
        assertEquals("home", out.getDescription());
        verify(addressRepository).save(a);
    }

    @Test
    void user_updateAddress_success() {
        Address existing = new Address(); existing.setId(10); existing.setUser(sampleUser); existing.setDescription("old");
        when(addressRepository.findById(10)).thenReturn(Optional.of(existing));
        Address update = new Address(); update.setUser(sampleUser); update.setDescription("new");
        when(addressRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Address out = userService.updateAddress(10, update);
        assertEquals("new", out.getDescription());
    }

    @Test
    void user_saveUser_hashesPassword() {
        User u = new User(); u.setPassword("p");
        when(passwordEncoder.encode("p")).thenReturn("h");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        User out = userService.saveUser(u);
        assertEquals("h", out.getPassword());
    }

    @Test
    void cart_createCart_invalidProduct_throws() {
        CartItem ci = new CartItem(); Product ref = new Product(); ref.setId(99); ci.setProduct(ref); ci.setQuantity(1);
        Cart c = new Cart(); c.setItems(List.of(ci));
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(c));
    }

    @Test
    void cart_updateCart_existing_updates() {
        Cart existing = new Cart(); existing.setId(5); existing.setFinalPrice(1.0f);
        Cart updated = new Cart(); updated.setFinalPrice(9.0f); updated.setUser(sampleUser);
        when(cartRepository.findById(5)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Cart out = cartService.updateCart(5, updated);
        assertEquals(9.0f, out.getFinalPrice());
    }

    @Test
    void cart_getEmailFromToken_delegatesToJwt() {
        // CartServiceImpl.getEmailFromToken delegates to jwtUtil; mock jwtUtil via injection
        JwtUtil jwtUtil = mock(JwtUtil.class);
        cartService = new CartServiceImpl();
        // inject mocks manually
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "jwtUtil", jwtUtil);
        when(jwtUtil.extractUsername("t1")).thenReturn("a@b.com");
        assertEquals("a@b.com", cartService.getEmailFromToken("t1"));
    }

    @Test
    void purchase_save_pending_setsReservationAndEmits() throws Exception {
        Purchase p = new Purchase(); p.setId(1); p.setStatus(Purchase.Status.PENDING);
        when(objectMapper.writeValueAsString(p)).thenReturn("{}");
        when(purchaseRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "objectMapper", objectMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "ecommerceEventService", ecommerceEventService);

        Purchase out = purchaseService.save(p);
        assertNotNull(out.getReservationTime());
        verify(ecommerceEventService).emitRawEvent(anyString(), anyString());
    }

    @Test
    void purchase_confirmPurchase_changesStatus() {
        Purchase p = new Purchase(); p.setId(2); p.setStatus(Purchase.Status.PENDING);
        when(purchaseRepository.findById(2)).thenReturn(Optional.of(p));
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
        Purchase out = purchaseService.confirmPurchase(2);
        assertEquals(Purchase.Status.CONFIRMED, out.getStatus());
    }

    @Test
    void purchase_deleteById_setsCancelled() {
        Purchase p = new Purchase(); p.setId(3); p.setStatus(Purchase.Status.PENDING);
        when(purchaseRepository.findById(3)).thenReturn(Optional.of(p));
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
        purchaseService.deleteById(3);
        assertEquals(Purchase.Status.CANCELLED, p.getStatus());
        verify(purchaseRepository).save(p);
    }

    @Test
    void passwordReset_requestPasswordReset_success_savesToken() {
        when(userRepository.findByEmail("u@example.com")).thenReturn(sampleUser);
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        org.springframework.test.util.ReflectionTestUtils.setField(passwordResetService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(passwordResetService, "tokenRepository", tokenRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(passwordResetService, "mailSender", mailSender);

        assertDoesNotThrow(() -> passwordResetService.requestPasswordReset("u@example.com"));
        verify(tokenRepository).save(any());
    }

    @Test
    void auth_registerDTO_createsUserAndSavesToken() {
        // Prepare DTO
        ar.edu.uade.ecommerce.Entity.DTO.RegisterUserDTO dto = new ar.edu.uade.ecommerce.Entity.DTO.RegisterUserDTO();
        dto.setName("N"); dto.setLastname("L"); dto.setEmail("x@y.com"); dto.setPassword("pw");
        when(passwordEncoder.encode("pw")).thenReturn("h");
        when(userRepository.save(any())).thenAnswer(i -> { User u = i.getArgument(0); u.setId(99); return u; });
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "tokenRepository", tokenRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "mailSender", mailSender);

        User saved = authService.registerDTO(dto);
        assertEquals(99, saved.getId());
        verify(tokenRepository).save(any());
    }

    @Test
    void address_findById_returnsAddress() {
        Address a = new Address(); a.setId(12); a.setDescription("d");
        when(addressRepository.findById(12)).thenReturn(Optional.of(a));
        org.springframework.test.util.ReflectionTestUtils.setField(addressService, "addressRepository", addressRepository);
        Address out = addressService.findById(12);
        assertEquals("d", out.getDescription());
    }

    @Test
    void purchase_getPurchasesByUserEmail_returnsEmptyWhenNoUser() {
        when(userRepository.findByEmail("no@ex.com")).thenReturn(null);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "userRepository", userRepository);
        List<PurchaseInvoiceDTO> res = purchaseService.getPurchasesByUserEmail("no@ex.com");
        assertTrue(res.isEmpty());
    }

    @Test
    void cart_revertProductStock_withNullItems_doesNotThrow() {
        Cart c = new Cart(); c.setItems(null);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "productRepository", productRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(cartService, "ecommerceEventService", ecommerceEventService);
        assertDoesNotThrow(() -> cartService.revertProductStock(c));
    }
}
