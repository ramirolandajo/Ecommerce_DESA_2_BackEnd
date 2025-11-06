package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.*;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoreServiceBatch20_2Test {

    @Mock
    CartItemRepository cartItemRepository;
    @Mock
    BrandRepository brandRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    PurchaseRepository purchaseRepository;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    ECommerceEventService ecommerceEventService;

    @InjectMocks
    CartItemServiceImpl cartItemService;
    @InjectMocks
    BrandServiceImpl brandService;
    @InjectMocks
    CategoryServiceImpl categoryService;
    @InjectMocks
    ProductServiceImpl productService;
    @InjectMocks
    PurchaseServiceImpl purchaseService;

    private CartItem item1;
    private CartItem item2;

    @BeforeEach
    void setUp() {
        item1 = new CartItem(); item1.setId(1); Cart c = new Cart(); c.setId(10); item1.setCart(c);
        item2 = new CartItem(); item2.setId(2); Cart c2 = new Cart(); c2.setId(20); item2.setCart(c2);
    }

    @Test
    void cartItem_findByCartId_filtersCorrectly() {
        when(cartItemRepository.findAll()).thenReturn(List.of(item1, item2));
        List<CartItem> res = cartItemService.getCartItemsByCartId(10);
        assertEquals(1, res.size());
        assertEquals(1, res.get(0).getId());
    }

    @Test
    void cartItem_findById_returnsNullIfMissing() {
        when(cartItemRepository.findById(99)).thenReturn(Optional.empty());
        assertNull(cartItemService.findById(99));
    }

    @Test
    void cartItem_delete_callsRepository() {
        doNothing().when(cartItemRepository).deleteById(1);
        cartItemService.delete(1);
        verify(cartItemRepository).deleteById(1);
    }

    @Test
    void brand_saveAll_withEmptyList_justDeletesExisting() {
        Brand b = new Brand(); b.setId(1); b.setName("X");
        when(brandRepository.findAll()).thenReturn(List.of(b), List.of());
        List<Brand> out = brandService.saveAllBrands(List.of());
        assertNotNull(out);
        verify(brandRepository).delete(b);
    }

    @Test
    void brand_saveBrand_nullName_returnsNull() {
        assertNull(brandService.saveBrand(null));
    }

    @Test
    void category_getAllActive_returnsFromRepo() {
        Category cat = new Category(); cat.setId(1); cat.setActive(true);
        when(categoryRepository.findByActiveTrue()).thenReturn(List.of(cat));
        List<Category> out = categoryService.getAllActiveCategories();
        assertEquals(1, out.size());
    }

    @Test
    void product_findById_notFound_throws() {
        when(productRepository.findById(5)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.findById(5L));
    }

    @Test
    void purchase_releaseExpiredReservations_handlesEmptyList() {
        when(purchaseRepository.findExpiredWithCartAndUser(eq(Purchase.Status.PENDING), any(LocalDateTime.class))).thenReturn(List.of());
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "objectMapper", objectMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "ecommerceEventService", ecommerceEventService);
        assertDoesNotThrow(() -> purchaseService.releaseExpiredReservations());
    }

    @Test
    void purchase_getPurchasesWithCartByUserId_filtersOnlyConfirmed() {
        Purchase p1 = new Purchase(); p1.setId(1); p1.setStatus(Purchase.Status.CONFIRMED);
        Purchase p2 = new Purchase(); p2.setId(2); p2.setStatus(Purchase.Status.PENDING);
        when(purchaseRepository.findByUser_Id(7)).thenReturn(List.of(p1, p2));
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
        List<?> res = purchaseService.getPurchasesWithCartByUserId(7);
        assertEquals(1, res.size());
    }

    @Test
    void purchase_findLastPendingPurchaseByUserWithinHours_returnsNullWhenExpired() {
        Purchase p = new Purchase(); p.setId(10); p.setStatus(Purchase.Status.PENDING);
        p.setReservationTime(LocalDateTime.now().minusHours(10));
        when(purchaseRepository.findByUser_IdAndStatusOrderByReservationTimeDesc(8, Purchase.Status.PENDING)).thenReturn(List.of(p));
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
        Purchase out = purchaseService.findLastPendingPurchaseByUserWithinHours(8, 4);
        assertNull(out);
    }

    @Test
    void purchase_findByUserId_returnsList() {
        Purchase p = new Purchase(); p.setId(20);
        when(purchaseRepository.findByUser_Id(9)).thenReturn(List.of(p));
        org.springframework.test.util.ReflectionTestUtils.setField(purchaseService, "purchaseRepository", purchaseRepository);
        List<Purchase> out = purchaseService.findByUserId(9);
        assertEquals(1, out.size());
    }

    @Test
    void product_updateStock_negativeStock_setsValue() {
        Product prod = new Product(); prod.setId(2); prod.setStock(5);
        when(productRepository.findById(2)).thenReturn(Optional.of(prod));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Product out = productService.updateProductStock(2, -3);
        assertEquals(-3, out.getStock());
    }

    @Test
    void category_saveAll_withNullIncoming_throwsNpe() {
        when(categoryRepository.findAll()).thenReturn(List.of());
        assertThrows(NullPointerException.class, () -> categoryService.saveAllCategories(null));
    }

    @Test
    void brand_getAllActiveBrands_returnsDtos() {
        Brand b = new Brand(); b.setId(3); b.setActive(true); b.setName("Z"); b.setBrandCode(999);
        when(brandRepository.findByActiveTrue()).thenReturn(List.of(b));
        Collection<?> out = brandService.getAllActiveBrands();
        assertEquals(1, out.size());
    }

    @Test
    void cartItem_findByCartId_repoMethodDelegates() {
        when(cartItemRepository.findByCartId(10)).thenReturn(List.of(item1));
        List<CartItem> out = cartItemService.findByCartId(10);
        assertEquals(1, out.size());
    }
}
