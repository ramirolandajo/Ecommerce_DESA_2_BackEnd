package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplExtraTests {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void testUpdateProductStock_whenSaveThrowsException() {
        Product product = new Product();
        product.setId(1);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenThrow(new RuntimeException("Save error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> productService.updateProductStock(1, 20));
        assertEquals("Save error", ex.getMessage());
    }

    @Test
    void testFindById_whenRepositoryThrowsException() {
        when(productRepository.findById(1)).thenThrow(new RuntimeException("DB error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> productService.findById(1L));
        assertEquals("DB error", ex.getMessage());
    }

    @Test
    void testFindById_withLargeId() {
        // Assuming int max is fine, but test with long that fits
        when(productRepository.findById(2147483647)).thenReturn(Optional.of(new Product()));
        Product result = productService.findById(2147483647L);
        assertNotNull(result);
    }

    @Test
    void testUpdateProductStock_withNegativeStock() {
        Product product = new Product();
        product.setId(1);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Product updated = productService.updateProductStock(1, -5);
        assertEquals(-5, updated.getStock());
    }

    @Test
    void testUpdateProductStock_withZeroStock() {
        Product product = new Product();
        product.setId(1);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Product updated = productService.updateProductStock(1, 0);
        assertEquals(0, updated.getStock());
    }

    @Test
    void testUpdateProductStock_whenFindByIdThrowsException() {
        when(productRepository.findById(1)).thenThrow(new RuntimeException("Find error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> productService.updateProductStock(1, 20));
        assertEquals("Find error", ex.getMessage());
    }

    @Test
    void testFindById_withIdTooLarge_throwsArithmeticException() {
        // Long value > Integer.MAX_VALUE
        long largeId = (long) Integer.MAX_VALUE + 1;
        ArithmeticException ex = assertThrows(ArithmeticException.class, () -> productService.findById(largeId));
        assertTrue(ex.getMessage().contains("integer overflow"));
    }

    @Test
    void testFindById_withNegativeId() {
        // Negative long that fits int
        when(productRepository.findById(-1)).thenReturn(Optional.of(new Product()));
        Product result = productService.findById(-1L);
        assertNotNull(result);
    }

    @Test
    void testFindById_withIdZero() {
        when(productRepository.findById(0)).thenReturn(Optional.of(new Product()));
        Product result = productService.findById(0L);
        assertNotNull(result);
    }
}
