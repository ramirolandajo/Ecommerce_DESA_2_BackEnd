package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.DTO.*;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Entity.Review;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import ar.edu.uade.ecommerce.Repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private KafkaMockService kafkaMockService;
    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEditProductSimple_Success() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        KafkaMockService.EditProductSimpleMessage msg = mock(KafkaMockService.EditProductSimpleMessage.class);
        KafkaMockService.EditProductSimplePayload payload = new KafkaMockService.EditProductSimplePayload(1L, 5, 95f);
        when(kafkaMockService.getEditProductMockSimple()).thenReturn(msg);
        when(msg.payload).thenReturn(payload);
        Product product = new Product();
        product.setId(1);
        product.setPrice(100f);
        product.setStock(10);
        product.setDiscount(5f);
        product.setPriceUnit(100f);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = controller.editProductSimple();
        assertEquals(5, result.getStock());
        assertEquals(95f, result.getPrice());
        assertEquals(5f, result.getDiscount());
    }

    @Test
    void testEditProductSimple_ProductNotFound() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        KafkaMockService.EditProductSimpleMessage msg = mock(KafkaMockService.EditProductSimpleMessage.class);
        KafkaMockService.EditProductSimplePayload payload = new KafkaMockService.EditProductSimplePayload(99L, 0, 0f);
        when(kafkaMockService.getEditProductMockSimple()).thenReturn(msg);
        when(msg.payload).thenReturn(payload);
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, controller::editProductSimple);
    }

    @Test
    void testAddReview_Success() {
        Product product = new Product();
        product.setId(5);
        product.setTitle("Producto Test");
        when(productRepository.findById(5)).thenReturn(Optional.of(product));
        Review review = new Review();
        review.setId(1L);
        review.setProduct(product);
        review.setCalification(4.5f);
        review.setDescription("Muy bueno");
        ProductController.ReviewRequest reviewRequest = new ProductController.ReviewRequest();
        reviewRequest.setCalification(4.5f);
        reviewRequest.setDescription("Muy bueno");
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewRepository.findByProduct(product)).thenReturn(List.of(review));
        productController.kafkaMockService = kafkaMockService;
        ProductController.ReviewResponse response = productController.addReview(5, reviewRequest);
        assertEquals(5, response.getProductId());
        assertEquals("Producto Test", response.getProductTitle());
        assertEquals(4.5f, response.getPromedio());
        assertEquals(1, response.getReviews().size());
        assertEquals("Muy bueno", response.getReviews().get(0).getDescription());
    }

    @Test
    void testAddReview_ProductNotFound() {
        ProductController.ReviewRequest reviewRequest = new ProductController.ReviewRequest();
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productController.addReview(99, reviewRequest));
    }

    @Test
    void testGetReviews_Success() {
        Product product = new Product();
        product.setId(6);
        product.setTitle("Producto Review");
        when(productRepository.findById(6)).thenReturn(Optional.of(product));
        Review review1 = new Review();
        review1.setId(1L);
        review1.setProduct(product);
        review1.setCalification(5f);
        review1.setDescription("Excelente");
        Review review2 = new Review();
        review2.setId(2L);
        review2.setProduct(product);
        review2.setCalification(3f);
        review2.setDescription("Bueno");
        when(reviewRepository.findByProduct(product)).thenReturn(List.of(review1, review2));
        ProductController.ReviewResponse response = productController.getReviews(6);
        assertEquals(6, response.getProductId());
        assertEquals("Producto Review", response.getProductTitle());
        assertEquals(4f, response.getPromedio());
        assertEquals(2, response.getReviews().size());
    }

    @Test
    void testGetReviews_ProductNotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productController.getReviews(99));
    }

    @Test
    void testSyncProductsFromMock() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        controller.entityManager = null; // No se usa en este test
        ProductDTO mockDto = new ProductDTO();
        mockDto.setTitle("Mock Product");
        mockDto.setStock(10);
        mockDto.setPrice(100f);
        Product product = new Product();
        product.setId(1); // Asigna un id válido para evitar NullPointerException
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(mockDto));
        KafkaMockService.ProductSyncMessage mockMessage = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(mockMessage);
        when(productRepository.findAll()).thenReturn(List.of(product));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(product);
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
        assertEquals(1, result.get(0).getId());
    }


    @Test
    void testAddProduct_Success() {
        // Ahora el método addProduct() no recibe argumentos, usa el mock
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        KafkaMockService.AddProductMessage msg = mock(KafkaMockService.AddProductMessage.class);
        ProductDTO dto = new ProductDTO();
        dto.setTitle("Nuevo producto");
        dto.setDescription("Desc");
        dto.setPrice(100f);
        dto.setStock(10);
        when(kafkaMockService.getAddProductMock()).thenReturn(msg);
        when(msg.payload).thenReturn(new KafkaMockService.AddProductPayload(dto));
        Product product = new Product();
        product.setId(7);
        product.setTitle("Nuevo producto");
        product.setDescription("Desc");
        product.setPrice(100f);
        product.setStock(10);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = controller.addProduct();
        assertEquals("Nuevo producto", result.getTitle());
        assertEquals(100f, result.getPrice());
        assertEquals(10, result.getStock());
    }

    @Test
    void testSyncProductsFromMock_BrandNotFound() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        controller.entityManager = mock(jakarta.persistence.EntityManager.class);
        ProductDTO mockDto = new ProductDTO();
        mockDto.setTitle("Mock Product");
        mockDto.setStock(10);
        mockDto.setPrice(100f);
        BrandDTO brandDTO = new BrandDTO();
        brandDTO.setId(99L);
        mockDto.setBrand(brandDTO);
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(mockDto));
        KafkaMockService.ProductSyncMessage mockMessage = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(mockMessage);
        when(productRepository.findAll()).thenReturn(List.of());
        doNothing().when(productRepository).deleteAll();
        when(controller.entityManager.find(Brand.class, 99)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> controller.syncProductsFromMock());
    }

    @Test
    void testSyncProductsFromMock_CategoryNotFound() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        controller.entityManager = mock(jakarta.persistence.EntityManager.class);
        ProductDTO mockDto = new ProductDTO();
        mockDto.setTitle("Mock Product");
        mockDto.setStock(10);
        mockDto.setPrice(100f);
        CategoryDTO catDTO = new CategoryDTO();
        catDTO.setId(88L);
        mockDto.setCategories(List.of(catDTO));
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(mockDto));
        KafkaMockService.ProductSyncMessage mockMessage = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(mockMessage);
        when(productRepository.findAll()).thenReturn(List.of());
        doNothing().when(productRepository).deleteAll();
        when(controller.entityManager.find(Category.class, 88)).thenReturn(null);
        // No lanza excepción, solo ignora la categoría nula
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
    }

    @Test
    void testToDTO_NullValues() {
        Product product = new Product();
        product.setId(8);
        product.setTitle(null);
        product.setDescription(null);
        product.setPrice(null);
        product.setStock(null); // Corregido: stock puede ser null para probar toDTO con valores nulos
        product.setMediaSrc(null);
        product.setBrand(null);
        product.setCategories(null);
        product.setIsNew(null);
        product.setIsBestseller(false);
        product.setIsFeatured(false);
        product.setHero(false);
        product.setCalification(null);
        product.setDiscount(null);
        product.setPriceUnit(null);
        product.setProductCode(null);
        product.setActive(null);
        ProductDTO dto = productController.toDTO(product);
        assertNull(dto.getTitle());
        assertNull(dto.getDescription());
        assertNull(dto.getPrice());
        assertNull(dto.getStock());
        assertNull(dto.getMediaSrc());
        assertNull(dto.getBrand());
        assertNull(dto.getCategories());
        assertNull(dto.getCalification());
        assertNull(dto.getDiscount());
        assertNull(dto.getPriceUnit());
        assertNull(dto.getProductCode());
        assertNull(dto.getActive());
    }

    @Test
    void testReviewDTO_Getters() {
        ProductController.ReviewDTO dto = new ProductController.ReviewDTO(10L, 4.5f, "Excelente");
        assertEquals(10L, dto.getId());
        assertEquals(4.5f, dto.getCalification());
        assertEquals("Excelente", dto.getDescription());
    }

    @Test
    void testToDTO_BrandAndCategoriesPresent() {
        Product product = new Product();
        product.setId(20);
        product.setTitle("Prod");
        product.setDescription("Desc");
        product.setPrice(10f);
        product.setStock(5);
        product.setMediaSrc(List.of("img1.jpg", "img2.jpg"));
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("MarcaTest");
        brand.setActive(true);
        product.setBrand(brand);
        Category cat = new Category();
        cat.setId(2);
        cat.setName("CatTest");
        cat.setActive(true);
        product.setCategories(Set.of(cat));
        product.setIsNew(true);
        product.setIsBestseller(true);
        product.setIsFeatured(true);
        product.setHero(true);
        product.setCalification(4.5f);
        product.setDiscount(2f);
        product.setPriceUnit(12f);
        product.setProductCode(12345);
        product.setActive(true);
        ProductDTO dto = productController.toDTO(product);
        assertEquals("Prod", dto.getTitle());
        assertEquals("Desc", dto.getDescription());
        assertEquals(10f, dto.getPrice());
        assertEquals(5, dto.getStock());
        assertEquals(2, dto.getMediaSrc().size());
        assertNotNull(dto.getBrand());
        assertEquals("MarcaTest", dto.getBrand().getName());
        assertNotNull(dto.getCategories());
        assertEquals("CatTest", dto.getCategories().get(0).getName());
        assertTrue(dto.getIsNew());
        assertTrue(dto.getIsBestseller());
        assertTrue(dto.getIsFeatured());
        assertTrue(dto.getHero());
        assertEquals(4.5f, dto.getCalification());
        assertEquals(2f, dto.getDiscount());
        assertEquals(12f, dto.getPriceUnit());
        assertEquals(12345, dto.getProductCode());
        assertTrue(dto.getActive());
    }

    @Test
    void testSyncProductsFromMock_BooleanFields() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        controller.entityManager = null;
        ProductDTO mockDto = new ProductDTO();
        mockDto.setTitle("MockBool");
        mockDto.setStock(1);
        mockDto.setPrice(10f);
        mockDto.setIsNew(true);
        mockDto.setIsBestseller(true);
        mockDto.setIsFeatured(true);
        mockDto.setHero(true);
        mockDto.setActive(false);
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(mockDto));
        KafkaMockService.ProductSyncMessage mockMessage = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(mockMessage);
        when(productRepository.findAll()).thenReturn(List.of(new Product()));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
    }

    @Test
    void testSyncProductsFromMock_MediaSrcNullAndEmpty() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        controller.entityManager = null;
        ProductDTO dtoNull = new ProductDTO();
        dtoNull.setTitle("NullMedia");
        dtoNull.setMediaSrc(null);
        ProductDTO dtoEmpty = new ProductDTO();
        dtoEmpty.setTitle("EmptyMedia");
        dtoEmpty.setMediaSrc(List.of());
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(dtoNull, dtoEmpty));
        KafkaMockService.ProductSyncMessage mockMessage = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(mockMessage);
        when(productRepository.findAll()).thenReturn(List.of(new Product(), new Product()));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
    }

    @Test
    void testSyncProductsFromMock_ActiveNullAndNotNull() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        controller.entityManager = null;
        ProductDTO dtoNull = new ProductDTO();
        dtoNull.setTitle("NullActive");
        dtoNull.setActive(null);
        ProductDTO dtoTrue = new ProductDTO();
        dtoTrue.setTitle("TrueActive");
        dtoTrue.setActive(true);
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(dtoNull, dtoTrue));
        KafkaMockService.ProductSyncMessage mockMessage = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(mockMessage);
        when(productRepository.findAll()).thenReturn(List.of(new Product(), new Product()));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
    }

    @Test
    void testSyncProductsFromMock_BrandAndCategoriesNull() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        controller.entityManager = null;
        ProductDTO dto = new ProductDTO();
        dto.setTitle("SinBrandCat");
        dto.setBrand(null);
        dto.setCategories(null);
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(dto));
        KafkaMockService.ProductSyncMessage mockMessage = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(mockMessage);
        when(productRepository.findAll()).thenReturn(List.of(new Product()));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
    }

    @Test
    void testSyncProductsFromMock_CategoryIdNull() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        controller.entityManager = mock(jakarta.persistence.EntityManager.class);
        ProductDTO dto = new ProductDTO();
        dto.setTitle("CatIdNull");
        CategoryDTO catDTO = new CategoryDTO();
        catDTO.setId(null);
        dto.setCategories(List.of(catDTO));
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(dto));
        KafkaMockService.ProductSyncMessage mockMessage = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(mockMessage);
        when(productRepository.findAll()).thenReturn(List.of(new Product()));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
    }

    @Test
    void testSyncProductsFromMock_EmptyCategoriesList() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        controller.entityManager = null;
        ProductDTO dto = new ProductDTO();
        dto.setTitle("EmptyCatList");
        dto.setCategories(List.of());
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(dto));
        KafkaMockService.ProductSyncMessage mockMessage = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(mockMessage);
        when(productRepository.findAll()).thenReturn(List.of(new Product()));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
    }

    @Test
    void testSyncProductsFromMock_EmptyMock() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        controller.entityManager = null;
        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of());
        KafkaMockService.ProductSyncMessage mockMessage = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getProductsMock()).thenReturn(mockMessage);
        when(productRepository.findAll()).thenReturn(List.of());
        doNothing().when(productRepository).deleteAll();
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testEditProductFromMock() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        KafkaMockService.EditProductFullMessage msg = mock(KafkaMockService.EditProductFullMessage.class);
        ProductDTO dto = new ProductDTO();
        dto.setId(2L);
        dto.setTitle("New title");
        dto.setStock(20);
        dto.setPriceUnit(200f);
        dto.setDiscount(20f);
        when(kafkaMockService.getEditProductMockFull()).thenReturn(msg);
        // Asignar el campo payload directamente
        msg.payload = (KafkaMockService.EditProductFullPayload) dto;
        Product product = new Product();
        product.setId(2);
        product.setTitle("Old title");
        product.setStock(10);
        product.setPriceUnit(100f);
        product.setDiscount(10f);
        when(productRepository.findById(2)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = controller.editProduct();
        assertEquals("New title", result.getTitle());
        assertEquals(20, result.getStock());
        assertEquals(200f, result.getPriceUnit());
        assertEquals(20f, result.getDiscount());
    }

    @Test
    void testActivateProductFromMock() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        KafkaMockService.ActivateProductMessage msg = mock(KafkaMockService.ActivateProductMessage.class);
        KafkaMockService.ActivateProductPayload payload = new KafkaMockService.ActivateProductPayload(3L);
        when(kafkaMockService.getActivateProductMock()).thenReturn(msg);
        when(msg.payload).thenReturn(payload);
        Product product = new Product();
        product.setId(3);
        product.setActive(false);
        when(productRepository.findById(3)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = controller.activateProduct();
        assertTrue(result.getActive());
    }

    @Test
    void testDeactivateProductFromMock() {
        ProductController controller = new ProductController();
        controller.kafkaMockService = kafkaMockService;
        controller.productRepository = productRepository;
        KafkaMockService.DeactivateProductMessage msg = mock(KafkaMockService.DeactivateProductMessage.class);
        KafkaMockService.DeactivateProductPayload payload = new KafkaMockService.DeactivateProductPayload(4L);
        when(kafkaMockService.getDeactivateProductMock()).thenReturn(msg);
        when(msg.payload).thenReturn(payload);
        Product product = new Product();
        product.setId(4);
        product.setActive(true);
        when(productRepository.findById(4)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = controller.deactivateProduct();
        assertFalse(result.getActive());
    }
}
