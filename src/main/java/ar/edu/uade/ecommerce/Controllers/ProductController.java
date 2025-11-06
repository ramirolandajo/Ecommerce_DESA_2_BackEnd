package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.DTO.BrandDTO;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.Entity.DTO.FilterProductRequest;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Entity.DTO.ProductDTO;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import ar.edu.uade.ecommerce.Entity.Review;
import ar.edu.uade.ecommerce.Repository.ReviewRepository;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    ar.edu.uade.ecommerce.Repository.CartItemRepository cartItemRepository;
    @Autowired
    ar.edu.uade.ecommerce.Repository.BrandRepository brandRepository;
    @Autowired
    ar.edu.uade.ecommerce.Repository.CategoryRepository categoryRepository;
    @Autowired
    ar.edu.uade.ecommerce.Repository.FavouriteProductsRepository favouriteProductsRepository;
    @Autowired
    ar.edu.uade.ecommerce.Repository.UserRepository userRepository;
    @Autowired
    ar.edu.uade.ecommerce.Service.AuthService authService;
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private ECommerceEventService ecommerceEventService;

    // Sincroniza productos desde el mock -> DESACTIVADO: usar la API de Comunicación para sincronizar
    @Transactional(timeout = 60)
    @GetMapping("/sync")
    public List<ProductDTO> syncProductsFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint de sincronización mock deshabilitado. Use la API de Comunicación para enviar eventos de producto.");
    }

    // Obtiene todos los productos con paginación
    @GetMapping
    public org.springframework.data.domain.Page<ProductDTO> getAllProducts(org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Product> page = productRepository.findAll(pageable);
        return page.map(this::toDTO);
    }

    // Agrega un producto específico usando mensaje mockeado -> DESACTIVADO
    @PostMapping
    public ProductDTO addProduct() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Los productos deben agregarse a través de eventos desde el Core.");
    }

    // Edita solo precio y stock usando mensaje mockeado -> DESACTIVADO
    public static class EditProductSimpleRequest { public Long id; }

    @PatchMapping("/simple")
    public ProductDTO editProductSimple() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use eventos desde el Core para actualizar productos.");
    }

    // Edita el producto completo usando mensaje mockeado -> DESACTIVADO
    @PatchMapping
    public ProductDTO editProduct() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use eventos desde el Core para actualizar productos.");
    }

    // Activar producto usando mensaje mockeado -> DESACTIVADO
    @PatchMapping("/activate")
    public ProductDTO activateProduct() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use eventos desde el Core para activar productos.");
    }

    // Desactivar producto usando mensaje mockeado -> DESACTIVADO
    @PatchMapping("/deactivate")
    public ProductDTO deactivateProduct() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use eventos desde el Core para desactivar productos.");
    }

    // DTO para request de review
    public static class ReviewRequest {
        private float calification;
        private String description;
        public float getCalification() { return calification; }
        public void setCalification(float calification) { this.calification = calification; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    // DTO para response de reviews
    public static class ReviewDTO {
        private Long id;
        private float calification;
        private String description;
        public ReviewDTO(Long id, float calification, String description) {
            this.id = id;
            this.calification = calification;
            this.description = description;
        }
        public Long getId() { return id; }
        public float getCalification() { return calification; }
        public String getDescription() { return description; }
    }

    public static class ReviewResponse {
        private Integer productId;
        private String productTitle;
        private float promedio;
        private List<ReviewDTO> reviews;
        public ReviewResponse(Integer productId, String productTitle, float promedio, List<ReviewDTO> reviews) {
            this.productId = productId;
            this.productTitle = productTitle;
            this.promedio = promedio;
            this.reviews = reviews;
        }
        public Integer getProductId() { return productId; }
        public String getProductTitle() { return productTitle; }
        public float getPromedio() { return promedio; }
        public List<ReviewDTO> getReviews() { return reviews; }
    }

    // Califica un producto (ahora crea una review)
    @PostMapping("/code/{productCode}/review")
    public ReviewResponse addReview(@PathVariable Integer productCode, @RequestBody ReviewRequest reviewRequest) {
        Product product = productRepository.findByProductCode(productCode);
        if (product == null) throw new RuntimeException("Producto no encontrado por productCode=" + productCode);
        // Obtener usuario autenticado
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        ar.edu.uade.ecommerce.Entity.User user = null;
        if (auth != null) {
            String email = auth.getName();
            user = userRepository.findByEmail(email);
            if (user == null) throw new RuntimeException("Usuario no encontrado");
            // Verificar si ya existe una review de este usuario para este producto
            Review existingReview = reviewRepository.findByProductAndUser(product, user);
            if (existingReview != null) {
                throw new RuntimeException("Ya has calificado/comentado este producto.");
            }
        }
        Review review = new Review();
        review.setProduct(product);
        if (user != null) review.setUser(user);
        review.setCalification(reviewRequest.getCalification());
        review.setDescription(reviewRequest.getDescription());
        reviewRepository.save(review);
        List<Review> reviews = reviewRepository.findByProduct(product);
        float promedio = (float) reviews.stream().mapToDouble(Review::getCalification).average().orElse(0.0);
        // Actualizar el campo calification en Product
        product.setCalification(promedio);
        productRepository.save(product);
        // Emitir evento hacia la API de Comunicación (Core)
        try {
            String message = reviewRequest.getDescription() != null ? reviewRequest.getDescription() : "Nueva review creada";
            Float rateUpdated = promedio;
            ecommerceEventService.emitReviewCreated(product.getProductCode(), message, rateUpdated);
        } catch (Exception ex) {
            System.err.println("Error emitiendo evento de review: " + ex.getMessage());
        }
        List<ReviewDTO> reviewDTOs = reviews.stream()
            .map(r -> new ReviewDTO(r.getId(), r.getCalification(), r.getDescription()))
            .collect(Collectors.toList());
        return new ReviewResponse(product.getId(), product.getTitle(), promedio, reviewDTOs);
    }

    //obtiene los reviews de un producto de un usuario
    @GetMapping("/code/{productCode}/review/me")
    public ReviewDTO getMyReview(@PathVariable Integer productCode) {
        Product product = productRepository.findByProductCode(productCode);
        if (product == null) throw new RuntimeException("Producto no encontrado por productCode=" + productCode);
        // Obtener usuario autenticado
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new RuntimeException("Usuario no autenticado");
        String email = auth.getName();
        ar.edu.uade.ecommerce.Entity.User user = userRepository.findByEmail(email);
        if (user == null) throw new RuntimeException("Usuario no encontrado");
        Review review = reviewRepository.findByProductAndUser(product, user);
        if (review == null) throw new RuntimeException("No has calificado/comentado este producto.");
        return new ReviewDTO(review.getId(), review.getCalification(), review.getDescription());
    }

    // Obtiene el promedio y listado de reviews de un producto
    @GetMapping("/{id}/reviews")
    public ReviewResponse getReviews(@PathVariable Integer id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        List<Review> reviews = reviewRepository.findByProduct(product);
        float promedio = (float) reviews.stream().mapToDouble(Review::getCalification).average().orElse(0.0);
        List<ReviewDTO> reviewDTOs = reviews.stream()
            .map(r -> new ReviewDTO(r.getId(), r.getCalification(), r.getDescription()))
            .collect(Collectors.toList());
        return new ReviewResponse(product.getId(), product.getTitle(), promedio, reviewDTOs);
    }

    // Filtrado de productos por categoría, marca y rango de precio
    @PostMapping("/filter")
    public org.springframework.data.domain.Page<ProductDTO> filterProducts(@RequestBody FilterProductRequest filterRequest) {
        List<Product> products = productRepository.findAll();
        // Filtrado
        List<Product> filtered = products.stream()
            .filter(p -> {
                boolean matches = true;
                if (filterRequest.getPriceMin() != null && (p.getPrice() == null || p.getPrice() < filterRequest.getPriceMin())) {
                    matches = false;
                }
                if (filterRequest.getPriceMax() != null && (p.getPrice() == null || p.getPrice() > filterRequest.getPriceMax())) {
                    matches = false;
                }
                // Filtrado por múltiples brandCodes
                if (filterRequest.getBrandCodes() != null && !filterRequest.getBrandCodes().isEmpty()) {
                    if (p.getBrand() == null || !filterRequest.getBrandCodes().contains(p.getBrand().getBrandCode())) {
                        matches = false;
                    }
                } else if (filterRequest.getBrandCode() != null) {
                    if (p.getBrand() == null || !filterRequest.getBrandCode().equals(p.getBrand().getBrandCode())) {
                        matches = false;
                    }
                }
                // Filtrado por múltiples categoryCodes
                if (filterRequest.getCategoryCodes() != null && !filterRequest.getCategoryCodes().isEmpty()) {
                    if (p.getCategories() == null || p.getCategories().stream().noneMatch(c -> filterRequest.getCategoryCodes().contains(c.getCategoryCode()))) {
                        matches = false;
                    }
                } else if (filterRequest.getCategoryCode() != null) {
                    if (p.getCategories() == null || p.getCategories().stream().noneMatch(c -> filterRequest.getCategoryCode().equals(c.getCategoryCode()))) {
                        matches = false;
                    }
                }
                return matches;
            })
            .collect(Collectors.toList());
        // Ordenamiento
        if (filterRequest.getSortBy() != null) {
            if ("price".equalsIgnoreCase(filterRequest.getSortBy())) {
                if ("desc".equalsIgnoreCase(filterRequest.getSortOrder())) {
                    filtered.sort((a, b) -> Float.compare(b.getPrice(), a.getPrice()));
                } else {
                    filtered.sort((a, b) -> Float.compare(a.getPrice(), b.getPrice()));
                }
            } else if ("relevance".equalsIgnoreCase(filterRequest.getSortBy())) {
                if ("desc".equalsIgnoreCase(filterRequest.getSortOrder())) {
                    filtered.sort((a, b) -> Float.compare(b.getCalification(), a.getCalification()));
                } else {
                    filtered.sort((a, b) -> Float.compare(a.getCalification(), b.getCalification()));
                }
            }
        }
        // Paginación manual
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 20;
        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        List<ProductDTO> pageContent = filtered.subList(start, end).stream().map(this::toDTO).collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(pageContent, org.springframework.data.domain.PageRequest.of(page, size), filtered.size());
    }

    // Obtener producto por id y productos relacionados
    @GetMapping("/{id}")
    public ProductWithRelatedDTO getProductById(@PathVariable("id") Long id) {
        Optional<Product> productOpt = productRepository.findById(id.intValue());
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        ProductDTO mainProduct = toDTO(product);
        List<Product> allProducts = productRepository.findAll();
        // Filtrar productos relacionados por marca o categoría (excluyendo el mismo producto)
        List<ProductDTO> related = allProducts.stream()
            .filter(p -> !p.getId().equals(product.getId()) && (
                (product.getBrand() != null && p.getBrand() != null && p.getBrand().getId().equals(product.getBrand().getId())) ||
                (product.getCategories() != null && p.getCategories() != null && p.getCategories().stream().anyMatch(cat -> product.getCategories().stream().anyMatch(pc -> pc.getId().equals(cat.getId()))) )
            ))
            .map(this::toDTO)
            .collect(Collectors.toList());
        return new ProductWithRelatedDTO(mainProduct, related);
    }

    // DTO para producto con relacionados
    public static class ProductWithRelatedDTO {
        public ProductDTO product;
        public List<ProductDTO> relatedProducts;
        public ProductWithRelatedDTO(ProductDTO product, List<ProductDTO> relatedProducts) {
            this.product = product;
            this.relatedProducts = relatedProducts;
        }
        public ProductDTO getProduct() { return product; }
        public List<ProductDTO> getRelatedProducts() { return relatedProducts; }
    }

    // Conversión a DTO
    ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId() != null ? Long.valueOf(product.getId()) : null);
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setMediaSrc(product.getMediaSrc());
        dto.setCalification(product.getCalification());
        dto.setDiscount(product.getDiscount());
        dto.setPriceUnit(product.getPriceUnit());
        dto.setProductCode(product.getProductCode());
        // Si el campo active es null, setear null en el DTO
        dto.setActive(product.getActive() != null ? product.getActive() : null);
        // Devuelve el nombre de la marca y categorías
        if (product.getBrand() != null) {
            dto.setBrand(new BrandDTO(Long.valueOf(product.getBrand().getId()), product.getBrand().getName(), product.getBrand().isActive()));
        } else {
            dto.setBrand(null);
        }
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            dto.setCategories(product.getCategories().stream()
                .map(cat -> new CategoryDTO(Long.valueOf(cat.getId()), cat.getName(), cat.isActive()))
                .collect(Collectors.toList()));
        } else {
            dto.setCategories(null);
        }
        dto.setIsNew(product.getIsNew());
        dto.setIsBestseller(product.isIsBestseller());
        dto.setIsFeatured(product.isIsFeatured());
        dto.setHero(product.isHero());
        return dto;
    }

    // Obtener productos destacados para el home screen
    @GetMapping("/homescreen")
    public List<ProductDTO> getHomeScreenProducts() {
        return productRepository.findAll().stream()
            .filter(p -> Boolean.TRUE.equals(p.isIsBestseller())
                || Boolean.TRUE.equals(p.isHero())
                || Boolean.TRUE.equals(p.isIsFeatured())
                || Boolean.TRUE.equals(p.getIsNew())
                || (p.getDiscount() != null && p.getDiscount() > 0))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // Agrega un producto a favoritos
    @PostMapping("/favourite/{productCode}")
    public String addFavouriteProduct(@PathVariable Integer productCode) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        ar.edu.uade.ecommerce.Entity.User user = userRepository.findByEmail(email);
        if (user == null) return "Usuario no encontrado.";
        Product product = productRepository.findByProductCode(productCode);
        if (product == null) return "Producto no encontrado.";
        // Verificar si ya es favorito
        if (favouriteProductsRepository.findByUserAndProduct(user, product).isPresent()) {
            return "El producto ya está en favoritos.";
        }
        // Guardar favorito
        ar.edu.uade.ecommerce.Entity.FavouriteProducts fav = new ar.edu.uade.ecommerce.Entity.FavouriteProducts();
        fav.setUser(user);
        fav.setProduct(product);
        fav.setProductCode(productCode); // Guardar el productCode en la entidad
        favouriteProductsRepository.save(fav);
        // Emitir evento hacia la API de Comunicación (Core) para agregar favorito
        try {
            ecommerceEventService.emitAddFavorite(String.valueOf(productCode), product.getId() != null ? Long.valueOf(product.getId()) : null, product.getTitle());
        } catch (Exception ex) {
            System.err.println("Error emitiendo evento add favorite: " + ex.getMessage());
        }
        return "El producto con código " + productCode + " se agregó a favoritos correctamente.";
    }

    // Quita un producto de favoritos
    @Transactional
    @DeleteMapping("/favourite/{productCode}")
    public String removeFavouriteProduct(@PathVariable Integer productCode) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        ar.edu.uade.ecommerce.Entity.User user = userRepository.findByEmail(email);
        if (user == null) return "Usuario no encontrado.";
        Product product = productRepository.findByProductCode(productCode);
        if (product == null) return "Producto no encontrado.";
        // Eliminar favorito si existe
        favouriteProductsRepository.deleteByUserAndProduct(user, product);
        // Emitir evento hacia la API de Comunicación (Core) para remover favorito
        try {
            ecommerceEventService.emitRemoveFavorite(String.valueOf(productCode), product.getId() != null ? Long.valueOf(product.getId()) : null, product.getTitle());
        } catch (Exception ex) {
            System.err.println("Error emitiendo evento remove favorite: " + ex.getMessage());
        }
        return "El producto con código " + productCode + " ya no es favorito.";
    }

    // Endpoint para traer productos favoritos del usuario autenticado
    @GetMapping("/user/favourite-products")
    public List<java.util.Map<String, Object>> getFavouriteProductsByUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        ar.edu.uade.ecommerce.Entity.User user = userRepository.findByEmail(email);
        if (user == null) return java.util.Collections.emptyList();
        return favouriteProductsRepository.findAllByUser(user).stream()
            .map(fav -> {
                Product p = fav.getProduct();
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", p.getId());
                map.put("title", p.getTitle());
                map.put("description", p.getDescription());
                map.put("mediaSrc", p.getMediaSrc());
                map.put("price", p.getPrice());
                map.put("stock", p.getStock());
                map.put("productCode", fav.getProductCode());
                return map;
            })
            .toList();
    }
}
