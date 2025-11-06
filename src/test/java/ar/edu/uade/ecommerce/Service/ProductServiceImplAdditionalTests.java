package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplAdditionalTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(5);
        product.setStock(10);
        product.setPrice(2.5f);
    }

    @Test
    void updateProductStock_success() {
        when(productRepository.findById(5)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProductStock(5, 3);

        assertNotNull(updated);
        assertEquals(3, updated.getStock());
        verify(productRepository).save(updated);
    }

    @Test
    void updateProductStock_notFound_throws() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> productService.updateProductStock(99, 1));
        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    @Test
    void findById_success() {
        when(productRepository.findById(5)).thenReturn(Optional.of(product));
        Product p = productService.findById(5L);
        assertNotNull(p);
        assertEquals(5, p.getId());
    }
}

