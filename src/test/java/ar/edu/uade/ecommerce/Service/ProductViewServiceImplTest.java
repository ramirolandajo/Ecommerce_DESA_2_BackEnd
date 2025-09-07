package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.DTO.ProductViewResponseDTO;
import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Repository.ProductViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProductViewServiceImplTest {
    @Mock
    private ProductViewRepository productViewRepository;

    @InjectMocks
    private ProductViewServiceImpl productViewServiceImpl;

    @Test
    void testSaveProductView() {
        User user = Mockito.mock(User.class);
        Product product = Mockito.mock(Product.class);
        ProductView view = new ProductView(user, product, LocalDateTime.now());
        Mockito.when(productViewRepository.save(Mockito.any())).thenReturn(view);
        ProductView result = productViewServiceImpl.saveProductView(user, product);
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(product, result.getProduct());
    }

    @Test
    void testGetProductViewsByUser() {
        User user = Mockito.mock(User.class);
        Product product = Mockito.mock(Product.class);
        ProductView view = new ProductView(user, product, LocalDateTime.now());
        Pageable pageable = PageRequest.of(0, 20);
        Page<ProductView> page = new PageImpl<>(Collections.singletonList(view));
        Mockito.when(productViewRepository.findByUser(user, pageable)).thenReturn(page);
        Page<ProductViewResponseDTO> result = productViewServiceImpl.getProductViewsByUser(user, pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testToDTO() {
        User user = Mockito.mock(User.class);
        Product product = Mockito.mock(Product.class);
        Mockito.when(product.getId()).thenReturn(1);
        Mockito.when(product.getTitle()).thenReturn("Producto");
        Mockito.when(product.getBrand()).thenReturn(null);
        // Mock de Category para evitar error en getCategories()
        Category category = Mockito.mock(Category.class);
        Mockito.when(category.getName()).thenReturn("CategoriaTest");
        Mockito.when(product.getCategories()).thenReturn(Collections.singleton(category));
        ProductView view = new ProductView(user, product, LocalDateTime.now());
        ProductViewResponseDTO dto = productViewServiceImpl.toDTO(view);
        assertNotNull(dto);
        assertEquals(1L, dto.getProductId());
        assertEquals("Producto", dto.getProductName());
    }

    @Test
    void testGetAllViews() {
        User user = Mockito.mock(User.class);
        Product product = Mockito.mock(Product.class);
        ProductView view = new ProductView(user, product, LocalDateTime.now());
        Mockito.when(productViewRepository.findAll()).thenReturn(Arrays.asList(view));
        List<ProductView> result = productViewServiceImpl.getAllViews();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testToDTOWithBrandAndCategories() {
        User user = Mockito.mock(User.class);
        Product product = Mockito.mock(Product.class);
        Category category1 = Mockito.mock(Category.class);
        Category category2 = Mockito.mock(Category.class);
        Mockito.when(category1.getName()).thenReturn("Categoria1");
        Mockito.when(category2.getName()).thenReturn("Categoria2");
        Set<Category> categories = Set.of(category1, category2);
        Brand brand = Mockito.mock(Brand.class);
        Mockito.when(brand.getName()).thenReturn("MarcaTest");
        Mockito.when(product.getId()).thenReturn(2);
        Mockito.when(product.getTitle()).thenReturn("Producto2");
        Mockito.when(product.getCategories()).thenReturn(categories);
        Mockito.when(product.getBrand()).thenReturn(brand);
        ProductView view = new ProductView(user, product, LocalDateTime.now());
        ProductViewResponseDTO dto = productViewServiceImpl.toDTO(view);
        assertNotNull(dto);
        assertEquals("MarcaTest", dto.getBrand());
        assertEquals(2, dto.getProductId());
        assertEquals(2, dto.getCategories().size());
        assertTrue(dto.getCategories().contains("Categoria1"));
        assertTrue(dto.getCategories().contains("Categoria2"));
    }

    @Test
    void testToDTONullBrandEmptyCategories() {
        User user = Mockito.mock(User.class);
        Product product = Mockito.mock(Product.class);
        Mockito.when(product.getId()).thenReturn(3);
        Mockito.when(product.getTitle()).thenReturn("Producto3");
        Mockito.when(product.getCategories()).thenReturn(Collections.emptySet());
        Mockito.when(product.getBrand()).thenReturn(null);
        ProductView view = new ProductView(user, product, LocalDateTime.now());
        ProductViewResponseDTO dto = productViewServiceImpl.toDTO(view);
        assertNotNull(dto);
        assertNull(dto.getBrand());
        assertEquals(3, dto.getProductId());
        assertTrue(dto.getCategories().isEmpty());
    }
}
