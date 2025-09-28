package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.ProductView;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Entity.DTO.ProductViewResponseDTO;
import ar.edu.uade.ecommerce.Repository.ProductViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductViewServiceImpl {
    @Autowired
    private ProductViewRepository productViewRepository;

    public ProductView saveProductView(User user, Product product) {
        ProductView view = new ProductView(user, product, LocalDateTime.now());
        return productViewRepository.save(view);
    }

    public Page<ProductViewResponseDTO> getProductViewsByUser(User user, Pageable pageable) {
        Page<ProductView> views = productViewRepository.findByUser(user, pageable);
        return views.map(this::toDTO);
    }

    public ProductViewResponseDTO toDTO(ProductView view) {
        Product product = view.getProduct();
        List<String> categories = product.getCategories().stream().map(c -> c.getName()).collect(Collectors.toList());
        String brand = product.getBrand() != null ? product.getBrand().getName() : null;
        return new ProductViewResponseDTO(
                view.getId(),
                product.getId(),
                product.getTitle(),
                categories,
                brand,
                view.getViewedAt()
        );
    }

    public List<ProductView> getAllViews() {
        return productViewRepository.findAll();
    }

    // Nuevo: obtener resumen de vistas (product id, title, productCode) dentro de una transacción
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getAllViewsSummary() {
        List<ProductView> all = productViewRepository.findAll();
        return all.stream().map(v -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("productViewId", v.getId());
            // Acceso a v.getProduct() ocurre dentro de la transacción para evitar LazyInitializationException
            if (v.getProduct() != null) {
                m.put("productId", v.getProduct().getId());
                m.put("productTitle", v.getProduct().getTitle());
                // Añadimos productCode al resumen
                m.put("productCode", v.getProduct().getProductCode());
            } else {
                m.put("productId", null);
                m.put("productTitle", null);
                m.put("productCode", null);
            }
            m.put("viewedAt", v.getViewedAt());
            return m;
        }).toList();
    }
}
