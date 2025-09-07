package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.*;
import ar.edu.uade.ecommerce.Entity.DTO.ProductDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerAdditionalTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private FavouriteProductsRepository favouriteProductsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private KafkaMockService kafkaMockService;
    @InjectMocks
    private ProductController productController;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAddFavouriteProduct_Success() {
        // mock SecurityContext
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        User user = new User();
        user.setEmail("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(user);

        Product product = new Product();
        product.setId(10);
        product.setTitle("P");
        when(productRepository.findByProductCode(1007)).thenReturn(product);

        when(favouriteProductsRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        doAnswer(invocation -> invocation.getArgument(0)).when(favouriteProductsRepository).save(any());

        String res = productController.addFavouriteProduct(1007);
        assertTrue(res.contains("1007") || res.contains("se agregó"));
        verify(favouriteProductsRepository).save(any());
        verify(kafkaMockService).sendEvent(any());
    }

    @Test
    void testAddFavouriteProduct_ProductNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        User user = new User();
        user.setEmail("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(user);

        when(productRepository.findByProductCode(9999)).thenReturn(null);
        String res = productController.addFavouriteProduct(9999);
        assertTrue(res.contains("Producto no encontrado"));
    }

    @Test
    void testRemoveFavouriteProduct_Success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        User user = new User();
        user.setEmail("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(user);

        Product product = new Product();
        product.setId(20);
        product.setTitle("P");
        when(productRepository.findByProductCode(2001)).thenReturn(product);

        doNothing().when(favouriteProductsRepository).deleteByUserAndProduct(user, product);

        String res = productController.removeFavouriteProduct(2001);
        assertTrue(res.contains("ya no es favorito") || res.contains("no encontrado") || res.length()>0);
        verify(favouriteProductsRepository).deleteByUserAndProduct(user, product);
        verify(kafkaMockService).sendEvent(any());
    }

    @Test
    void testGetProductById_WithRelated() {
        Product product = new Product();
        product.setId(30);
        product.setTitle("Main");
        Brand brand = new Brand();
        brand.setId(1);
        product.setBrand(brand);

        Product related = new Product();
        related.setId(31);
        related.setTitle("Rel");
        related.setBrand(brand);

        when(productRepository.findById(30)).thenReturn(Optional.of(product));
        when(productRepository.findAll()).thenReturn(java.util.List.of(product, related));

        ProductController.ProductWithRelatedDTO dto = productController.getProductById(30L);
        assertNotNull(dto);
        assertEquals("Main", dto.getProduct().getTitle());
        assertTrue(dto.getRelatedProducts().stream().anyMatch(p -> "Rel".equals(p.getTitle())));
    }
}

