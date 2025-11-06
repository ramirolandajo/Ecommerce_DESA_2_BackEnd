package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplNewTests {
    @Mock ProductRepository productRepository;
    @InjectMocks ProductServiceImpl service;

    @Test
    void updateProductStock_whenNotFound_throws() {
        when(productRepository.findById(9)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateProductStock(9, 10));
        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    @Test
    void updateProductStock_updatesAndSaves() {
        Product p = new Product(); p.setId(1); p.setStock(5);
        when(productRepository.findById(1)).thenReturn(Optional.of(p));
        when(productRepository.save(p)).thenReturn(p);
        Product out = service.updateProductStock(1, 42);
        assertEquals(42, out.getStock());
        verify(productRepository).save(p);
    }

    @Test
    void findById_wrapsIntAndDelegates() {
        Product p = new Product(); p.setId(7);
        when(productRepository.findById(7)).thenReturn(Optional.of(p));
        assertSame(p, service.findById(7L));
    }
}

