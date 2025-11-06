package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.ProductViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductViewServiceImplMoreTests {

    @Mock ProductViewRepository repo;
    @InjectMocks ProductViewServiceImpl service;

    @Test
    void toDTO_handlesNullCategoriesAndBrand() {
        User u = new User();
        Product p = new Product(); p.setId(1); p.setTitle("T"); p.setCategories(null); p.setBrand(null);
        ProductView v = new ProductView(u, p, LocalDateTime.now());
        var dto = service.toDTO(v);
        assertEquals(1, dto.getProductId());
        assertNotNull(dto.getCategories());
        assertTrue(dto.getCategories().isEmpty());
        assertNull(dto.getBrand());
    }

    @Test
    void getProductViewsByUser_mapsPage() {
        User u = new User(); Product p = new Product(); p.setId(2); p.setTitle("X");
        ProductView v = new ProductView(u, p, LocalDateTime.now());
        Page<ProductView> page = new PageImpl<>(List.of(v));
        when(repo.findByUser(eq(u), any())).thenReturn(page);
        Page<?> out = service.getProductViewsByUser(u, PageRequest.of(0, 10));
        assertEquals(1, out.getTotalElements());
    }

    @Test
    void getAllViewsSummary_mapsFields() {
        Product p = new Product(); p.setId(5); p.setTitle("TT"); p.setProductCode(111);
        User u = new User(); ProductView v = new ProductView(u, p, LocalDateTime.now());
        when(repo.findAll()).thenReturn(List.of(v));
        var list = service.getAllViewsSummary();
        assertEquals(1, list.size());
        var m = list.get(0);
        assertEquals(5, m.get("productId"));
        assertEquals("TT", m.get("productTitle"));
        assertEquals(111, m.get("productCode"));
    }
}

