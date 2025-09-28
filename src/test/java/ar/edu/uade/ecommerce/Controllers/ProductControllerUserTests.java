//package ar.edu.uade.ecommerce.Controllers;
//
//import ar.edu.uade.ecommerce.Entity.*;
//import ar.edu.uade.ecommerce.Entity.DTO.FilterProductRequest;
//import ar.edu.uade.ecommerce.Entity.DTO.ProductDTO;
//import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
//import ar.edu.uade.ecommerce.Repository.FavouriteProductsRepository;
//import ar.edu.uade.ecommerce.Repository.ProductRepository;
//import ar.edu.uade.ecommerce.Repository.UserRepository;
//import ar.edu.uade.ecommerce.Service.AuthService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class ProductControllerUserTests {
//    @Mock
//    private ProductRepository productRepository;
//    @Mock
//    private FavouriteProductsRepository favouriteProductsRepository;
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private AuthService authService;
//    @Mock
//    private KafkaMockService kafkaMockService;
//
//    @InjectMocks
//    private ProductController productController;
//
//    @Test
//    void testGetFavouriteProductsByUser_success() {
//        String token = "token";
//        String email = "user@test.com";
//        User user = new User();
//        user.setEmail(email);
//        Product p = new Product();
//        p.setId(11);
//        p.setTitle("FavProd");
//        p.setDescription("desc");
//        p.setPrice(9.9f);
//        p.setMediaSrc(List.of("img.jpg"));
//        FavouriteProducts fav = new FavouriteProducts();
//        fav.setUser(user);
//        fav.setProduct(p);
//        when(authService.getEmailFromToken(token)).thenReturn(email);
//        when(userRepository.findByEmail(email)).thenReturn(user);
//        when(favouriteProductsRepository.findAllByUser(user)).thenReturn(List.of(fav));
//
//        List<Map<String, Object>> res = productController.getFavouriteProductsByUser("Bearer " + token);
//        assertNotNull(res);
//        assertEquals(1, res.size());
//        Map<String, Object> m = res.get(0);
//        assertEquals(11, m.get("id"));
//        assertEquals("FavProd", m.get("title"));
//    }
//
//    @Test
//    void testFilterProducts_withCategoryBrandPriceRange() {
//        Brand brand = new Brand();
//        brand.setId(2);
//        Category cat = new Category();
//        cat.setId(5);
//        Product p = new Product();
//        p.setId(21);
//        p.setBrand(brand);
//        p.setCategories(Set.of(cat));
//        p.setPrice(50f);
//        when(productRepository.findAll()).thenReturn(List.of(p));
//
//        FilterProductRequest req = new FilterProductRequest();
//        req.setBrandId(2L);
//        req.setCategoryId(5L);
//        req.setPriceMin(40f);
//        req.setPriceMax(60f);
//
//        List<ar.edu.uade.ecommerce.Entity.DTO.ProductDTO> out = productController.filterProducts(req);
//        assertNotNull(out);
//        assertEquals(1, out.size());
//        assertEquals(21L, out.get(0).getId());
//    }
//
//    @Test
//    void testGetHomeScreenProducts_includesMatching() {
//        Product p1 = new Product();
//        p1.setId(31);
//        p1.setBestseller(true);
//        Product p2 = new Product();
//        p2.setId(32);
//        p2.setHero(false);
//        p2.setIsFeatured(false);
//        p2.setIsNew(false);
//        p2.setDiscount(0f);
//        Product p3 = new Product();
//        p3.setId(33);
//        p3.setDiscount(5f);
//        when(productRepository.findAll()).thenReturn(List.of(p1, p2, p3));
//
//        List<ar.edu.uade.ecommerce.Entity.DTO.ProductDTO> out = productController.getHomeScreenProducts();
//        assertNotNull(out);
//        // p1 and p3 should be included
//        assertTrue(out.stream().anyMatch(d -> d.getId() == 31L));
//        assertTrue(out.stream().anyMatch(d -> d.getId() == 33L));
//    }
//
//    @Test
//    void testSyncProductsFromMock_updateExisting() {
//        // existing product with same productCode should be updated
//        Product existing = new Product();
//        existing.setId(500);
//        existing.setProductCode(3001);
//        existing.setTitle("OldTitle");
//        when(productRepository.findAll()).thenReturn(List.of(existing));
//
//        ProductDTO dto = new ProductDTO();
//        dto.setProductCode(3001);
//        dto.setTitle("NewTitle");
//        dto.setPrice(20f);
//        dto.setStock(2);
//        KafkaMockService.ProductSyncPayload payload = new KafkaMockService.ProductSyncPayload(List.of(dto));
//        KafkaMockService.ProductSyncMessage msg = new KafkaMockService.ProductSyncMessage("ProductSync", payload, java.time.LocalDateTime.now().toString());
//        when(kafkaMockService.getProductsMock()).thenReturn(msg);
//        // simulate save updating the existing entity
//        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
//            Product p = invocation.getArgument(0);
//            existing.setTitle(p.getTitle());
//            existing.setPrice(p.getPrice());
//            existing.setStock(p.getStock());
//            return existing;
//        });
//
//        // run
//        List<ProductDTO> out = productController.syncProductsFromMock();
//        assertNotNull(out);
//        assertEquals(1, out.size());
//        assertEquals("NewTitle", out.get(0).getTitle());
//    }
//
//    @Test
//    void testGetProductById_RelatedByCategory() {
//        Category cat = new Category(); cat.setId(88); cat.setName("C");
//        Product main = new Product(); main.setId(600); main.setTitle("Main"); main.setCategories(Set.of(cat));
//        Product rel = new Product(); rel.setId(601); rel.setTitle("Rel"); rel.setCategories(Set.of(cat));
//        when(productRepository.findById(600)).thenReturn(Optional.of(main));
//        when(productRepository.findAll()).thenReturn(List.of(main, rel));
//
//        ProductController.ProductWithRelatedDTO dto = productController.getProductById(600L);
//        assertNotNull(dto);
//        assertEquals("Main", dto.getProduct().getTitle());
//        assertTrue(dto.getRelatedProducts().stream().anyMatch(p -> "Rel".equals(p.getTitle())));
//    }
//
//    @Test
//    void testGetFavouriteProductsByUser_UserNotFound() {
//        String token = "tokenX";
//        when(authService.getEmailFromToken(token)).thenReturn("nouser@test.com");
//        when(userRepository.findByEmail("nouser@test.com")).thenReturn(null);
//        List<Map<String, Object>> res = productController.getFavouriteProductsByUser("Bearer " + token);
//        assertNotNull(res);
//        assertTrue(res.isEmpty());
//    }
//}
