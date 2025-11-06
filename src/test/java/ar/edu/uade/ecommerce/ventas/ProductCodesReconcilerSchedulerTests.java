package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Repository.BrandRepository;
import ar.edu.uade.ecommerce.Repository.CategoryRepository;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductCodesReconcilerSchedulerTests {

    ProductRepository productRepository;
    BrandRepository brandRepository;
    CategoryRepository categoryRepository;
    ProductCodesReconcilerScheduler scheduler;

    @BeforeEach
    void setup() {
        productRepository = mock(ProductRepository.class);
        brandRepository = mock(BrandRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        scheduler = new ProductCodesReconcilerScheduler(productRepository, brandRepository, categoryRepository);

        ReflectionTestUtils.setField(scheduler, "placeholderBrandName", "UNBRANDED");
        ReflectionTestUtils.setField(scheduler, "placeholderCategoryName", "UNCATEGORIZED");

        when(brandRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(brandRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }



    @Test
    void reconcile_fills_missing_codes_and_activates_when_ready() {
        Brand b = new Brand();
        b.setName("ACME");
        // sin brandCode; el scheduler le asigna uno

        Category c = new Category();
        c.setName("Tools");
        // sin categoryCode

        Product p = new Product();
        p.setBrand(b);
        p.setCategories(new HashSet<>(Set.of(c)));
        p.setActive(false);

        when(productRepository.findAll()).thenReturn(List.of(p));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(brandRepository.save(any())).thenAnswer(inv -> {
            Brand saved = inv.getArgument(0);
            assertNotNull(saved.getBrandCode());
            return saved;
        });
        when(categoryRepository.save(any())).thenAnswer(inv -> {
            Category saved = inv.getArgument(0);
            assertNotNull(saved.getCategoryCode());
            return saved;
        });

        scheduler.reconcile();

        assertNotNull(b.getBrandCode());
        assertNotNull(c.getCategoryCode());
        assertTrue(p.getActive()); // ready => activado
    }
}

