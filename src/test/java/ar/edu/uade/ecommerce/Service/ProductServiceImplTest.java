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
class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void updateProductStock_success() {
        Product product = new Product();
        product.setId(1);
        product.setStock(10);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProductStock(1, 20);
        assertNotNull(updated);
        assertEquals(20, updated.getStock());
        verify(productRepository).findById(1);
        verify(productRepository).save(product);
    }

    @Test
    void updateProductStock_productNotFound() {
        when(productRepository.findById(2)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> productService.updateProductStock(2, 30));
        assertEquals("Producto no encontrado", ex.getMessage());
        verify(productRepository).findById(2);
        verify(productRepository, never()).save(any());
    }
}
