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
class ProductServiceImplMoreTests {

    @Mock ProductRepository productRepository;
    @InjectMocks ProductServiceImpl service;

    @Test
    void updateProductStock_whenNotFound_throws() {
        when(productRepository.findById(77)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateProductStock(77, 1));
    }

    @Test
    void findById_success() {
        Product p = new Product(); p.setId(9);
        when(productRepository.findById(9)).thenReturn(Optional.of(p));
        Product out = service.findById(9L);
        assertEquals(9, out.getId());
    }
}
