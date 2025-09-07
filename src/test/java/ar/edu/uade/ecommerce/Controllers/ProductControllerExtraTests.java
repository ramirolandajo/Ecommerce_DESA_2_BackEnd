package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Entity.DTO.BrandDTO;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.Entity.DTO.FilterProductRequest;
import ar.edu.uade.ecommerce.Entity.DTO.ProductDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerExtraTests {
    @Mock
    ProductRepository productRepository;
    @Mock
    KafkaMockService kafkaMockService;
    @InjectMocks
    ProductController controller;

    @Test
    void testEditProduct_discountOnly_recalculatesPrice() {
        // prepare existing product with priceUnit present
        Product product = new Product();
        product.setId(100);
        product.setPriceUnit(200f);
        product.setDiscount(10f);
        when(productRepository.findById(100)).thenReturn(Optional.of(product));

        // craft payload that changes discount only
        KafkaMockService.EditProductFullMessage msg = mock(KafkaMockService.EditProductFullMessage.class);
        KafkaMockService.EditProductFullPayload payload = new KafkaMockService.EditProductFullPayload(
            100,
            null, null, null, null, List.of(), null, List.of(), null, null, null, null, null, null,
            20f, // new discount
            null, // priceUnit unchanged
            null
        );
        when(kafkaMockService.getEditProductMockFull()).thenReturn(msg);
        msg.payload = payload;
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductDTO dto = controller.editProduct();
        assertNotNull(dto);
        // price should be recalculated only if both priceUnit and discount non-null
        // priceUnit is 200, discount becomes 20 => price = 200 - 200*20/100 = 160
        assertEquals(160f, dto.getPrice());
    }

    @Test
    void testEditProduct_priceUnitOnly_recalculatesPrice() {
        Product product = new Product();
        product.setId(101);
        product.setPriceUnit(150f);
        product.setDiscount(10f);
        when(productRepository.findById(101)).thenReturn(Optional.of(product));

        KafkaMockService.EditProductFullMessage msg = mock(KafkaMockService.EditProductFullMessage.class);
        KafkaMockService.EditProductFullPayload payload = new KafkaMockService.EditProductFullPayload(
            101,
            null, null, null, null, List.of(), null, List.of(), null, null, null, null, null, null,
            null, // discount unchanged
            300f, // new priceUnit
            null
        );
        when(kafkaMockService.getEditProductMockFull()).thenReturn(msg);
        msg.payload = payload;
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductDTO dto = controller.editProduct();
        assertNotNull(dto);
        // discount = 10, priceUnit = 300 => price = 300 - 300*10/100 = 270
        assertEquals(270f, dto.getPrice());
    }

    @Test
    void testSyncProductsFromMock_createNew_withEntityManager() {
        // prepare controller with a mocked EntityManager to return brand/category
        controller.entityManager = mock(EntityManager.class);
        ProductDTO prodDto = new ProductDTO();
        prodDto.setTitle("NewMock");
        prodDto.setProductCode(7777);
        BrandDTO b = new BrandDTO(); b.setId(5L);
        prodDto.setBrand(b);
        CategoryDTO c = new CategoryDTO(); c.setId(6L);
        prodDto.setCategories(List.of(c));
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(prodDto));
        KafkaMockService.ProductSyncMessage msg = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(msg);
        when(productRepository.findAll()).thenReturn(List.of());
        // entity manager returns brand/category with ids
        Brand foundBrand = new Brand(); foundBrand.setId(5);
        Category foundCat = new Category(); foundCat.setId(6);
        when(controller.entityManager.find(Brand.class, 5)).thenReturn(foundBrand);
        when(controller.entityManager.find(Category.class, 6)).thenReturn(foundCat);
        Product saved = new Product(); saved.setId(900); saved.setTitle("NewMock"); saved.setProductCode(7777);
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        // Ensure findAll returns the saved product when controller collects DTOs
        when(productRepository.findAll()).thenReturn(List.of(saved));

        List<ProductDTO> out = controller.syncProductsFromMock();
        assertNotNull(out);
        assertEquals(1, out.size());
        assertEquals(7777, out.get(0).getProductCode());
    }

    @Test
    void testFilterProducts_brandAndPriceRange() {
        Brand brand = new Brand(); brand.setId(2);
        Product p1 = new Product(); p1.setId(1000); p1.setBrand(brand); p1.setPrice(50f);
        Product p2 = new Product(); p2.setId(1001); p2.setBrand(brand); p2.setPrice(200f);
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        FilterProductRequest req = new FilterProductRequest();
        req.setBrandId(2L);
        req.setPriceMin(10f);
        req.setPriceMax(100f);

        var out = controller.filterProducts(req);
        assertNotNull(out);
        assertEquals(1, out.size());
        assertEquals(1000L, out.get(0).getId());
    }
}
