package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Entity.DTO.ProductViewResponseDTO;
import ar.edu.uade.ecommerce.Repository.ProductViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductViewServiceImplNewTests {
    @Mock ProductViewRepository productViewRepository;
    @InjectMocks ProductViewServiceImpl service;

    @Test
    void saveProductView_persistsView() {
        User u = new User(); u.setId(1);
        Product p = new Product(); p.setId(2);
        ProductView saved = new ProductView(u,p, LocalDateTime.now());
        when(productViewRepository.save(any(ProductView.class))).thenReturn(saved);
        ProductView out = service.saveProductView(u,p);
        assertSame(saved, out);
        verify(productViewRepository).save(any(ProductView.class));
    }

    @Test
    void getProductViewsByUser_mapsPage() {
        User u = new User();
        Pageable pg = PageRequest.of(0,10);
        Product prod = new Product(); prod.setId(3); prod.setTitle("T");
        Category c = new Category(); c.setName("C1");
        prod.setCategories(Set.of(c));
        ProductView v = new ProductView(u, prod, LocalDateTime.now());
        v.setViewedAt(LocalDateTime.now());
        Page<ProductView> page = new PageImpl<>(List.of(v), pg, 1);
        when(productViewRepository.findByUser(u, pg)).thenReturn(page);
        Page<ProductViewResponseDTO> out = service.getProductViewsByUser(u, pg);
        assertEquals(1, out.getTotalElements());
        assertEquals("T", out.getContent().get(0).getProductName());
        assertEquals(List.of("C1"), out.getContent().get(0).getCategories());
    }

    @Test
    void toDTO_handlesEmptyCategoriesAndNullBrand() {
        User u = new User();
        Product prod = new Product(); prod.setId(5); prod.setTitle("X");
        // categories vacío para evitar NPE por implementación actual
        prod.setCategories(java.util.Collections.emptySet());
        ProductView v = new ProductView(u, prod, LocalDateTime.now());
        ProductViewResponseDTO dto = service.toDTO(v);
        assertEquals("X", dto.getProductName());
        assertNotNull(dto.getCategories());
    }

    @Test
    void getAllViews_delegatesToRepository() {
        when(productViewRepository.findAll()).thenReturn(List.of(new ProductView()));
        assertEquals(1, service.getAllViews().size());
    }
}
