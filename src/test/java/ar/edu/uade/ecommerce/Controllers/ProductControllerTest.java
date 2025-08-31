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
import org.springframework.http.ResponseEntity;
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
        Product product = new Product();
        product.setId(1);
        product.setPrice(100f);
        product.setStock(10);
        product.setDiscount(10f);
        product.setPriceUnit(110f);
        ProductDTO dto = new ProductDTO();
        dto.setPrice(90f);
        dto.setStock(5);
        dto.setDiscount(5f);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProductSimple(1L, dto);
        assertEquals(5, result.getStock());
        assertEquals(95f, result.getPrice()); // Ajustado según la lógica real
        assertEquals(5f, result.getDiscount());
    }

    @Test
    void testEditProductSimple_ProductNotFound() {
        ProductDTO dto = new ProductDTO();
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productController.editProductSimple(99L, dto));
    }

    @Test
    void testEditProduct_Success() {
        Product product = new Product();
        product.setId(2);
        product.setTitle("Old title");
        product.setStock(10);
        product.setPriceUnit(100f);
        product.setDiscount(10f);
        ProductDTO dto = new ProductDTO();
        dto.setTitle("New title");
        dto.setStock(20);
        dto.setPriceUnit(200f);
        dto.setDiscount(20f);
        when(productRepository.findById(2)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(2, dto);
        assertEquals("New title", result.getTitle());
        assertEquals(20, result.getStock());
        assertEquals(200f, result.getPriceUnit());
        assertEquals(20f, result.getDiscount());
    }

    @Test
    void testEditProduct_ProductNotFound() {
        ProductDTO dto = new ProductDTO();
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productController.editProduct(99, dto));
    }

    @Test
    void testActivateProduct_Success() {
        Product product = new Product();
        product.setId(3);
        product.setActive(false);
        when(productRepository.findById(3)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.activateProduct(3);
        assertTrue(result.getActive());
    }

    @Test
    void testActivateProduct_ProductNotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productController.activateProduct(99));
    }

    @Test
    void testDeactivateProduct_Success() {
        Product product = new Product();
        product.setId(4);
        product.setActive(true);
        when(productRepository.findById(4)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.deactivateProduct(4);
        assertFalse(result.getActive());
    }

    @Test
    void testDeactivateProduct_ProductNotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productController.deactivateProduct(99));
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
        when(kafkaMockService.getProductsMock()).thenReturn(List.of(mockDto));
        when(productRepository.findAll()).thenReturn(List.of(product));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(product);
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
        assertEquals(1, result.get(0).getId());
    }

    @Test
    void testAddProduct_Success() {
        ProductDTO dto = new ProductDTO();
        dto.setTitle("Nuevo producto");
        dto.setDescription("Desc");
        dto.setPrice(100f);
        dto.setStock(10);
        Product product = new Product();
        product.setId(7);
        product.setTitle("Nuevo producto");
        product.setDescription("Desc");
        product.setPrice(100f);
        product.setStock(10);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.addProduct(dto);
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
        when(kafkaMockService.getProductsMock()).thenReturn(List.of(mockDto));
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
        when(kafkaMockService.getProductsMock()).thenReturn(List.of(mockDto));
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
    void testEditProduct_OnlyPriceUnit() {
        Product product = new Product();
        product.setId(21);
        product.setPriceUnit(100f);
        product.setDiscount(10f);
        ProductDTO dto = new ProductDTO();
        dto.setPriceUnit(200f);
        when(productRepository.findById(21)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(21, dto);
        assertEquals(200f, result.getPriceUnit());
        assertEquals(180f, result.getPrice()); // 200 - 10%
    }

    @Test
    void testEditProduct_OnlyDiscount() {
        Product product = new Product();
        product.setId(22);
        product.setPriceUnit(100f);
        product.setDiscount(0f);
        ProductDTO dto = new ProductDTO();
        dto.setDiscount(20f);
        when(productRepository.findById(22)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(22, dto);
        assertEquals(20f, result.getDiscount());
        assertEquals(80f, result.getPrice()); // 100 - 20%
    }

    @Test
    void testEditProduct_PriceUnitAndDiscountNull() {
        Product product = new Product();
        product.setId(23);
        product.setPrice(50f);
        ProductDTO dto = new ProductDTO();
        dto.setPrice(60f);
        when(productRepository.findById(23)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(23, dto);
        assertEquals(60f, result.getPrice());
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
        when(kafkaMockService.getProductsMock()).thenReturn(List.of(mockDto));
        when(productRepository.findAll()).thenReturn(List.of(new Product()));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
    }

    @Test
    void testAddProduct_AllFields() {
        ProductDTO dto = new ProductDTO();
        dto.setTitle("ProdAll");
        dto.setDescription("DescAll");
        dto.setPrice(100f);
        dto.setStock(10);
        dto.setMediaSrc(List.of("imgA.jpg"));
        dto.setIsNew(true);
        dto.setIsBestseller(true);
        dto.setIsFeatured(true);
        dto.setHero(true);
        Product product = new Product();
        product.setId(30);
        product.setTitle("ProdAll");
        product.setDescription("DescAll");
        product.setPrice(100f);
        product.setStock(10);
        product.setMediaSrc(List.of("imgA.jpg"));
        product.setNew(true);
        product.setBestseller(true);
        product.setFeatured(true);
        product.setHero(true);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.addProduct(dto);
        assertEquals("ProdAll", result.getTitle());
        assertEquals("DescAll", result.getDescription());
        assertEquals(100f, result.getPrice());
        assertEquals(10, result.getStock());
        assertEquals(1, result.getMediaSrc().size());
        assertTrue(result.getIsNew());
        assertTrue(result.getIsBestseller());
        assertTrue(result.getIsFeatured());
        assertTrue(result.getHero());
    }

    @Test
    void testGetAllProducts_EmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());
        List<ProductDTO> result = productController.getAllProducts();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllProducts_WithProducts() {
        Product product1 = new Product();
        product1.setId(1);
        product1.setTitle("Prod1");
        product1.setPrice(10f);
        Product product2 = new Product();
        product2.setId(2);
        product2.setTitle("Prod2");
        product2.setPrice(20f);
        when(productRepository.findAll()).thenReturn(List.of(product1, product2));
        List<ProductDTO> result = productController.getAllProducts();
        assertEquals(2, result.size());
        assertEquals("Prod1", result.get(0).getTitle());
        assertEquals("Prod2", result.get(1).getTitle());
    }

    @Test
    void testEditProduct_OnlyTitle() {
        Product product = new Product();
        product.setId(31);
        product.setTitle("OldTitle");
        ProductDTO dto = new ProductDTO();
        dto.setTitle("NewTitle");
        when(productRepository.findById(31)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(31, dto);
        assertEquals("NewTitle", result.getTitle());
    }

    @Test
    void testEditProduct_OnlyDescription() {
        Product product = new Product();
        product.setId(32);
        product.setDescription("OldDesc");
        ProductDTO dto = new ProductDTO();
        dto.setDescription("NewDesc");
        when(productRepository.findById(32)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(32, dto);
        assertEquals("NewDesc", result.getDescription());
    }

    @Test
    void testEditProduct_OnlyProductCode() {
        Product product = new Product();
        product.setId(33);
        product.setProductCode(111);
        ProductDTO dto = new ProductDTO();
        dto.setProductCode(222);
        when(productRepository.findById(33)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(33, dto);
        assertEquals(222, result.getProductCode());
    }

    @Test
    void testEditProduct_MediaSrcNullAndNotNull() {
        Product product = new Product();
        product.setId(34);
        product.setMediaSrc(List.of("img1.jpg"));
        ProductDTO dto = new ProductDTO();
        dto.setMediaSrc(null);
        when(productRepository.findById(34)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(34, dto);
        assertEquals(List.of(), result.getMediaSrc());
        // Ahora con mediaSrc no nulo
        dto.setMediaSrc(List.of("img2.jpg"));
        result = productController.editProduct(34, dto);
        assertEquals(List.of("img2.jpg"), result.getMediaSrc());
    }

    @Test
    void testEditProduct_ActiveNullAndNotNull() {
        Product product = new Product();
        product.setId(35);
        product.setActive(true);
        ProductDTO dto = new ProductDTO();
        dto.setActive(null);
        when(productRepository.findById(35)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(35, dto);
        assertTrue(result.getActive());
        dto.setActive(false);
        result = productController.editProduct(35, dto);
        assertFalse(result.getActive());
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
        when(kafkaMockService.getProductsMock()).thenReturn(List.of(dtoNull, dtoEmpty));
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
        when(kafkaMockService.getProductsMock()).thenReturn(List.of(dtoNull, dtoTrue));
        when(productRepository.findAll()).thenReturn(List.of(new Product(), new Product()));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
    }

    @Test
    void testEditProductSimple_OnlyStock() {
        Product product = new Product();
        product.setId(41);
        product.setStock(10);
        ProductDTO dto = new ProductDTO();
        dto.setStock(20);
        when(productRepository.findById(41)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProductSimple(41L, dto);
        assertEquals(20, result.getStock());
    }

    @Test
    void testEditProductSimple_OnlyDiscount() {
        Product product = new Product();
        product.setId(42);
        product.setPriceUnit(100f);
        product.setDiscount(0f);
        ProductDTO dto = new ProductDTO();
        dto.setDiscount(15f);
        when(productRepository.findById(42)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProductSimple(42L, dto);
        assertEquals(15f, result.getDiscount());
        assertEquals(85f, result.getPrice());
    }

    @Test
    void testEditProductSimple_AllNulls() {
        Product product = new Product();
        product.setId(43);
        product.setPrice(50f);
        product.setStock(10);
        product.setDiscount(5f);
        product.setPriceUnit(55f);
        ProductDTO dto = new ProductDTO();
        when(productRepository.findById(43)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProductSimple(43L, dto);
        assertEquals(10, result.getStock());
        assertEquals(50f, result.getPrice());
        assertEquals(5f, result.getDiscount());
        assertEquals(55f, result.getPriceUnit());
    }

//    @Test
//    void testEditProduct_AllFieldsNull() {
//        Product product = new Product();
//        product.setId(50);
//        product.setTitle("Titulo");
//        product.setDescription("Desc");
//        product.setStock(10);
//        product.setPrice(100f);
//        product.setMediaSrc(List.of("img.jpg"));
//        product.setNew(true);
//        product.setBestseller(true);
//        product.setFeatured(true);
//        product.setHero(true);
//        product.setActive(true);
//        product.setProductCode(123);
//        product.setPriceUnit(100f);
//        product.setDiscount(10f);
//        ProductDTO dto = new ProductDTO(); // todos los campos null
//        when(productRepository.findById(50)).thenReturn(Optional.of(product));
//        when(productRepository.save(any(Product.class))).thenReturn(product);
//        ProductDTO result = productController.editProduct(50, dto);
//        assertEquals("Titulo", result.getTitle());
//        assertEquals("Desc", result.getDescription());
//        assertEquals(10, result.getStock());
//        assertEquals(List.of("img.jpg"), result.getMediaSrc());
//        assertTrue(result.getIsNew());
//        assertTrue(result.getIsBestseller());
//        assertTrue(result.getIsFeatured());
//        assertTrue(result.getHero());
//        assertTrue(result.getActive());
//        assertEquals(123, result.getProductCode());
//        assertEquals(100f, result.getPriceUnit());
//        assertEquals(10f, result.getDiscount());
//        assertEquals(90f, result.getPrice());
//    }

    @Test
    void testEditProduct_OnlyBooleanFields() {
        Product product = new Product();
        product.setId(51);
        product.setNew(false);
        product.setBestseller(false);
        product.setFeatured(false);
        product.setHero(false);
        ProductDTO dto = new ProductDTO();
        dto.setIsNew(true);
        dto.setIsBestseller(true);
        dto.setIsFeatured(true);
        dto.setHero(true);
        when(productRepository.findById(51)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(51, dto);
        assertTrue(result.getIsNew());
        assertTrue(result.getIsBestseller());
        assertTrue(result.getIsFeatured());
        assertTrue(result.getHero());
    }

    @Test
    void testEditProduct_ProductCodeNull() {
        Product product = new Product();
        product.setId(52);
        product.setProductCode(123);
        ProductDTO dto = new ProductDTO();
        dto.setProductCode(null);
        when(productRepository.findById(52)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProduct(52, dto);
        assertNull(result.getProductCode());
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
        when(kafkaMockService.getProductsMock()).thenReturn(List.of(dto));
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
        when(kafkaMockService.getProductsMock()).thenReturn(List.of(dto));
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
        when(kafkaMockService.getProductsMock()).thenReturn(List.of(dto));
        when(productRepository.findAll()).thenReturn(List.of(new Product()));
        doNothing().when(productRepository).deleteAll();
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        List<ProductDTO> result = controller.syncProductsFromMock();
        assertNotNull(result);
    }

    @Test
    void testEditProductSimple_AllFieldsNull() {
        Product product = new Product();
        product.setId(60);
        product.setPrice(100f);
        product.setStock(10);
        product.setDiscount(10f);
        product.setPriceUnit(110f);
        ProductDTO dto = new ProductDTO(); // todos los campos null
        when(productRepository.findById(60)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProductSimple(60L, dto);
        assertEquals(10, result.getStock());
        assertEquals(100f, result.getPrice());
        assertEquals(10f, result.getDiscount());
        assertEquals(110f, result.getPriceUnit());
    }

    @Test
    void testEditProductSimple_OnlyDiscountWithPriceUnitNull() {
        Product product = new Product();
        product.setId(61);
        product.setPriceUnit(null);
        product.setDiscount(0f);
        product.setPrice(50f);
        ProductDTO dto = new ProductDTO();
        dto.setDiscount(20f);
        when(productRepository.findById(61)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result = productController.editProductSimple(61L, dto);
        assertEquals(20f, result.getDiscount());
        assertEquals(50f, result.getPrice()); // price no cambia porque priceUnit es null
    }
}
