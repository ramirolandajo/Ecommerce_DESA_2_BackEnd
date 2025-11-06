package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Repository.BrandRepository;
import ar.edu.uade.ecommerce.Repository.CategoryRepository;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VentasInventorySyncServiceTests {

    ProductRepository productRepository;
    BrandRepository brandRepository;
    CategoryRepository categoryRepository;
    ObjectMapper mapper;
    VentasInventorySyncService service;

    @BeforeEach
    void setup() {
        productRepository = mock(ProductRepository.class);
        brandRepository = mock(BrandRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        mapper = new ObjectMapper();
        service = new VentasInventorySyncService(productRepository, brandRepository, categoryRepository, mapper);
    }

    @Test
    void actualizarStock_creates_placeholder_when_not_found() {
        when(productRepository.findByProductCode(1001)).thenReturn(null);
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", 1001);
        payload.put("stock", 50);
        payload.put("nombre", "Prod A");

        service.actualizarStock(payload);

        verify(productRepository).save(captor.capture());
        Product p = captor.getValue();
        assertEquals(1001, p.getProductCode());
        assertEquals(50, p.getStock());
        assertTrue(p.getActive());
        assertEquals("Prod A", p.getTitle());
    }

    @Test
    void crearProducto_creates_brand_and_categories_when_missing() {
        when(productRepository.findByProductCode(2001)).thenReturn(null);
        when(brandRepository.findByBrandCode(10)).thenReturn(Optional.empty());
        when(brandRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(brandRepository.save(any())).thenAnswer(inv -> {
            Brand b = inv.getArgument(0);
            if (b.getId() == null) b.setId(1);
            return b;
        });
        when(categoryRepository.findByCategoryCode(anyInt())).thenReturn(Optional.empty());
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            if (c.getId() == null) c.setId(2);
            return c;
        });
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", 2001);
        payload.put("name", "Prod B");
        payload.put("brandCode", 10);
        payload.put("brandName", "Brand X");
        payload.put("categoryCodes", List.of(101, 102));
        payload.put("images", List.of("a.jpg", "b.jpg"));

        service.crearProducto(payload);

        verify(productRepository).save(captor.capture());
        Product p = captor.getValue();
        assertEquals(2001, p.getProductCode());
        assertNotNull(p.getBrand());
        assertTrue(p.getCategories() != null && !p.getCategories().isEmpty());
        assertEquals(2, p.getMediaSrc().size());
    }

    @Test
    void modificarProducto_updates_fields_only_when_present() {
        Product existing = new Product();
        existing.setId(7);
        existing.setProductCode(3001);
        existing.setTitle("Old");
        when(productRepository.findByProductCode(3001)).thenReturn(existing);

        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", 3001);
        payload.put("price", 123.45f);
        payload.put("stock", 9);

        service.modificarProducto(payload);

        verify(productRepository).save(existing);
        assertEquals(123.45f, existing.getPrice());
        assertEquals(9, existing.getStock());
        assertEquals("Old", existing.getTitle());
    }

    @Test
    void activar_y_desactivar_producto_by_id_or_code() {
        Product byCode = new Product();
        byCode.setProductCode(4001);
        when(productRepository.findByProductCode(4001)).thenReturn(byCode);
        Map<String, Object> payloadCode = Map.of("productCode", 4001);
        service.desactivarProducto(payloadCode);
        assertFalse(byCode.getActive());

        Product byId = new Product();
        byId.setId(55);
        when(productRepository.findByProductCode(5001)).thenReturn(null);
        when(productRepository.findById(55)).thenReturn(Optional.of(byId));
        Map<String, Object> payloadId = Map.of("productCode", 5001, "id", 55);
        service.activarProducto(payloadId);
        assertTrue(byId.getActive());
    }

    @Test
    void activarMarca_fallback_by_id_when_not_found_by_code_or_name() {
        when(brandRepository.findByBrandCode(anyInt())).thenReturn(Optional.empty());
        when(brandRepository.findByName(anyString())).thenReturn(Optional.empty());
        Brand b = new Brand(); b.setId(9); b.setActive(false);
        when(brandRepository.findById(9)).thenReturn(Optional.of(b));

        Map<String, Object> payload = Map.of("id", 9);
        service.activarMarca(payload);

        assertTrue(b.isActive());
        verify(brandRepository).save(b);
    }

    @Test
    void desactivarCategoria_by_name() {
        Category c = new Category(); c.setId(11); c.setActive(true); c.setName("CAT");
        when(categoryRepository.findByCategoryCode(anyInt())).thenReturn(Optional.empty());
        when(categoryRepository.findByName("CAT")).thenReturn(Optional.of(c));

        service.desactivarCategoria(Map.of("name", "CAT"));

        assertFalse(c.isActive());
        verify(categoryRepository).save(c);
    }

    @Test
    void crearProductosBatch_processes_multiple_items_even_with_errors() {
        when(productRepository.findByProductCode(anyInt())).thenReturn(null);
        when(brandRepository.findByBrandCode(anyInt())).thenReturn(Optional.empty());
        when(categoryRepository.findByCategoryCode(anyInt())).thenReturn(Optional.empty());
        when(brandRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> good = new HashMap<>();
        good.put("productCode", 6001);
        good.put("name", "OK");

        Map<String, Object> bad = new HashMap<>(); // sin productCode -> error
        bad.put("name", "BAD");

        Map<String, Object> payload = new HashMap<>();
        payload.put("items", List.of(good, bad));

        service.crearProductosBatch(payload);

        // Se guarda al menos el correcto
        verify(productRepository, atLeast(1)).save(any(Product.class));
    }
}

