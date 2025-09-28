//package ar.edu.uade.ecommerce.Controllers;
//
//import ar.edu.uade.ecommerce.Entity.Product;
//import ar.edu.uade.ecommerce.Entity.ProductView;
//import ar.edu.uade.ecommerce.Entity.User;
//import ar.edu.uade.ecommerce.Entity.DTO.ProductViewResponseDTO;
//import ar.edu.uade.ecommerce.Service.ProductViewServiceImpl;
//import ar.edu.uade.ecommerce.Service.AuthService;
//import ar.edu.uade.ecommerce.Service.ProductService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.http.ResponseEntity;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//
//public class ProductViewControllerTest {
//    @Mock
//    private ProductViewServiceImpl productViewServiceImpl;
//    @Mock
//    private ProductService productService;
//    @Mock
//    private AuthService authService;
//    @InjectMocks
//    private ProductViewController productViewController;
//    @Mock
//    private ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService kafkaMockService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testRegisterProductViewSuccess() {
//        User user = new User();
//        user.setEmail("test@email.com");
//        user.setSessionActive(true);
//        Product product = new Product();
//        product.setId(1);
//        product.setTitle("Producto");
//        ProductView view = new ProductView(user, product, LocalDateTime.now());
//        Mockito.when(authService.getEmailFromToken(any())).thenReturn(user.getEmail());
//        Mockito.when(authService.getUserByEmail(user.getEmail())).thenReturn(user);
//        Mockito.when(productService.findById(1L)).thenReturn(product);
//        Mockito.when(productViewServiceImpl.saveProductView(user, product)).thenReturn(view);
//        ResponseEntity<?> response = productViewController.registerProductView("Bearer token", 1L);
//        assertEquals(200, response.getStatusCodeValue());
//        assertTrue(response.getBody().toString().contains("test@email.com"));
//    }
//
//    @Test
//    void testRegisterProductViewUnauthorized() {
//        ResponseEntity<?> response = productViewController.registerProductView(null, 1L);
//        assertEquals(401, response.getStatusCodeValue());
//    }
//
//    @Test
//    void testGetProductViewsSuccess() {
//        User user = new User();
//        user.setEmail("test@email.com");
//        user.setSessionActive(true);
//        ProductViewResponseDTO dto = new ProductViewResponseDTO(1L, 1, "Producto", Collections.emptyList(), null, LocalDateTime.now());
//        Page<ProductViewResponseDTO> page = new PageImpl<>(Collections.singletonList(dto));
//        Mockito.when(authService.getEmailFromToken(any())).thenReturn(user.getEmail());
//        Mockito.when(authService.getUserByEmail(user.getEmail())).thenReturn(user);
//        Mockito.when(productViewServiceImpl.getProductViewsByUser(any(), any())).thenReturn(page);
//        ResponseEntity<Page<ProductViewResponseDTO>> response = productViewController.getProductViews("Bearer token", 0, 20);
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(1, response.getBody().getTotalElements());
//    }
//
//    @Test
//    void testGetProductViewsUnauthorized() {
//        ResponseEntity<Page<ProductViewResponseDTO>> response = productViewController.getProductViews(null, 0, 20);
//        assertEquals(401, response.getStatusCodeValue());
//    }
//
//    @Test
//    void testRegisterProductViewProductNotFound() {
//        User user = new User();
//        user.setEmail("test@email.com");
//        user.setSessionActive(true);
//        Mockito.when(authService.getEmailFromToken(any())).thenReturn(user.getEmail());
//        Mockito.when(authService.getUserByEmail(user.getEmail())).thenReturn(user);
//        Mockito.when(productService.findById(99L)).thenReturn(null);
//        ResponseEntity<?> response = productViewController.registerProductView("Bearer token", 99L);
//        assertEquals(404, response.getStatusCodeValue());
//        assertTrue(response.getBody().toString().contains("Producto no encontrado"));
//    }
//
//    @Test
//    void testRegisterProductViewInactiveSession() {
//        User user = new User();
//        user.setEmail("test@email.com");
//        user.setSessionActive(false);
//        Mockito.when(authService.getEmailFromToken(any())).thenReturn(user.getEmail());
//        Mockito.when(authService.getUserByEmail(user.getEmail())).thenReturn(user);
//        ResponseEntity<?> response = productViewController.registerProductView("Bearer token", 1L);
//        assertEquals(401, response.getStatusCodeValue());
//        assertTrue(response.getBody().toString().contains("Usuario no logueado"));
//    }
//
//    @Test
//    void testRegisterProductViewInvalidToken() {
//        Mockito.when(authService.getEmailFromToken(any())).thenReturn(null);
//        ResponseEntity<?> response = productViewController.registerProductView("Bearer token", 1L);
//        assertEquals(401, response.getStatusCodeValue());
//        assertTrue(response.getBody().toString().contains("Usuario no logueado"));
//    }
//
//    @Test
//    void testRegisterProductViewUserNotFound() {
//        Mockito.when(authService.getEmailFromToken(any())).thenReturn("test@email.com");
//        Mockito.when(authService.getUserByEmail(any())).thenReturn(null);
//        ResponseEntity<?> response = productViewController.registerProductView("Bearer token", 1L);
//        assertEquals(401, response.getStatusCodeValue());
//        assertTrue(response.getBody().toString().contains("Usuario no logueado"));
//    }
//
//    @Test
//    void testGetProductViewsInactiveSession() {
//        User user = new User();
//        user.setEmail("test@email.com");
//        user.setSessionActive(false);
//        Mockito.when(authService.getEmailFromToken(any())).thenReturn(user.getEmail());
//        Mockito.when(authService.getUserByEmail(user.getEmail())).thenReturn(user);
//        ResponseEntity<Page<ProductViewResponseDTO>> response = productViewController.getProductViews("Bearer token", 0, 20);
//        assertEquals(401, response.getStatusCodeValue());
//    }
//
//    @Test
//    void testGetProductViewsInvalidToken() {
//        Mockito.when(authService.getEmailFromToken(any())).thenReturn(null);
//        ResponseEntity<Page<ProductViewResponseDTO>> response = productViewController.getProductViews("Bearer token", 0, 20);
//        assertEquals(401, response.getStatusCodeValue());
//    }
//
//    @Test
//    void testGetProductViewsUserNotFound() {
//        Mockito.when(authService.getEmailFromToken(any())).thenReturn("test@email.com");
//        Mockito.when(authService.getUserByEmail(any())).thenReturn(null);
//        ResponseEntity<Page<ProductViewResponseDTO>> response = productViewController.getProductViews("Bearer token", 0, 20);
//        assertEquals(401, response.getStatusCodeValue());
//    }
//
//    @Test
//    void testSendDailyProductViewEvent() {
//        User user = Mockito.mock(User.class);
//        Mockito.when(user.getEmail()).thenReturn("test@email.com");
//        Product product = Mockito.mock(Product.class);
//        Mockito.when(product.getTitle()).thenReturn("Producto");
//        ProductView view = Mockito.mock(ProductView.class);
//        Mockito.when(view.getUser()).thenReturn(user);
//        Mockito.when(view.getProduct()).thenReturn(product);
//        Mockito.when(view.getViewedAt()).thenReturn(LocalDateTime.now());
//        List<ProductView> views = Collections.singletonList(view);
//        Mockito.when(productViewServiceImpl.getAllViews()).thenReturn(views);
//        assertDoesNotThrow(() -> productViewController.sendDailyProductViewEvent());
//        Mockito.verify(productViewServiceImpl).getAllViews();
//        Mockito.verify(kafkaMockService).sendEvent(any());
//    }
//
//    @Test
//    void testGetProductViewsJsonProcessingException() {
//        User user = new User();
//        user.setEmail("test@email.com");
//        user.setSessionActive(true);
//        ProductViewResponseDTO dto = new ProductViewResponseDTO(1L, 1, "Producto", Collections.emptyList(), null, LocalDateTime.now());
//        Page<ProductViewResponseDTO> page = new PageImpl<>(Collections.singletonList(dto));
//        Mockito.when(authService.getEmailFromToken(any())).thenReturn(user.getEmail());
//        Mockito.when(authService.getUserByEmail(user.getEmail())).thenReturn(user);
//        Mockito.when(productViewServiceImpl.getProductViewsByUser(any(), any())).thenReturn(page);
//        // Simular excepción en ObjectMapper
//        ObjectMapper mapper = Mockito.mock(ObjectMapper.class);
//        try {
//            Mockito.when(mapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Mocked error") {});
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        // Reemplazar temporalmente la creación de ObjectMapper en el controlador si es posible
//        // Si no, este test no puede cubrir el catch sin refactorizar el controlador para inyectar ObjectMapper
//        ResponseEntity<Page<ProductViewResponseDTO>> response = productViewController.getProductViews("Bearer token", 0, 20);
//        assertEquals(200, response.getStatusCodeValue());
//        // No lanza excepción, solo loguea el error
//    }
//}
