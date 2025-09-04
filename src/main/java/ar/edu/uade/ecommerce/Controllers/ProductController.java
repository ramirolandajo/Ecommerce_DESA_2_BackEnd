package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.DTO.BrandDTO;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.Entity.DTO.FilterProductRequest;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Entity.DTO.ProductDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import ar.edu.uade.ecommerce.Entity.Review;
import ar.edu.uade.ecommerce.Repository.ReviewRepository;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    KafkaMockService kafkaMockService;
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
    @PersistenceContext
    EntityManager entityManager;

    // Sincroniza productos desde el mock
    @Transactional(timeout = 60)
    @GetMapping("/sync")
    public List<ProductDTO> syncProductsFromMock() {
        KafkaMockService.ProductSyncMessage message = kafkaMockService.getProductsMock();
        List<ProductDTO> mockProducts = message.payload.products;
        for (ProductDTO dto : mockProducts) {
            if (dto.getProductCode() == null) continue;
            Product existing = productRepository.findAll().stream()
                .filter(p -> p.getProductCode() != null && p.getProductCode().equals(dto.getProductCode()))
                .findFirst().orElse(null);
            StringBuilder errorMsg = new StringBuilder();
            Brand brandEntity = null;
            if (dto.getBrand() != null && dto.getBrand().getId() != null) {
                brandEntity = brandRepository.findById((long) dto.getBrand().getId().intValue()).orElse(null);
                if (brandEntity == null) {
                    errorMsg.append("Marca no encontrada (ID: " + dto.getBrand().getId() + ") para producto: " + dto.getTitle() + " (productCode: " + dto.getProductCode() + "). ");
                }
            }
            Set<Category> cats = null;
            if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
                cats = dto.getCategories().stream()
                    .map(catDto -> categoryRepository.findById((long) catDto.getId().intValue()).orElse(null))
                    .collect(java.util.stream.Collectors.toSet());
                for (Category c : cats) {
                    if (c == null) {
                        errorMsg.append("Categoría no encontrada para producto: " + dto.getTitle() + " (productCode: " + dto.getProductCode() + "). ");
                    }
                }
            }
            if (errorMsg.length() > 0) {
                throw new RuntimeException(errorMsg.toString());
            }
            // Al asignar mediaSrc, usar nueva lista mutable
            List<String> mediaSrcMutable = dto.getMediaSrc() != null ? new java.util.ArrayList<>(dto.getMediaSrc()) : new java.util.ArrayList<>();
            if (existing == null) {
                Product product = new Product();
                product.setTitle(dto.getTitle());
                product.setDescription(dto.getDescription());
                product.setPrice(dto.getPrice());
                product.setStock(dto.getStock());
                product.setMediaSrc(mediaSrcMutable);
                product.setNew(dto.getIsNew() != null ? dto.getIsNew() : false);
                product.setBestseller(dto.getIsBestseller() != null ? dto.getIsBestseller() : false);
                product.setFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
                product.setHero(dto.getHero() != null ? dto.getHero() : false);
                product.setActive(dto.getActive() != null ? dto.getActive() : true);
                product.setDiscount(dto.getDiscount());
                product.setPriceUnit(dto.getPriceUnit());
                product.setProductCode(dto.getProductCode());
                product.setBrand(brandEntity);
                if (cats != null) {
                    product.setCategories(new java.util.HashSet<>(cats.stream().filter(c -> c != null).toList()));
                }
                product.setCalification(dto.getCalification() != null ? dto.getCalification() : 0f);
                productRepository.save(product);
            } else {
                existing.setTitle(dto.getTitle());
                existing.setDescription(dto.getDescription());
                existing.setPrice(dto.getPrice());
                existing.setStock(dto.getStock());
                existing.setMediaSrc(mediaSrcMutable);
                existing.setNew(dto.getIsNew() != null ? dto.getIsNew() : false);
                existing.setBestseller(dto.getIsBestseller() != null ? dto.getIsBestseller() : false);
                existing.setFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
                existing.setHero(dto.getHero() != null ? dto.getHero() : false);
                existing.setActive(dto.getActive() != null ? dto.getActive() : true);
                existing.setDiscount(dto.getDiscount());
                existing.setPriceUnit(dto.getPriceUnit());
                existing.setProductCode(dto.getProductCode());
                existing.setBrand(brandEntity);
                if (cats != null) {
                    existing.setCategories(new java.util.HashSet<>(cats.stream().filter(c -> c != null).toList()));
                } else {
                    existing.setCategories(null);
                }
                existing.setCalification(dto.getCalification() != null ? dto.getCalification() : 0f);
                productRepository.save(existing);
            }
        }
        // Retornar todos los productos como DTO
        return productRepository.findAll().stream()
            .map(ProductDTO::fromEntity)
            .collect(Collectors.toList());
    }

    // Obtiene todos los productos
    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // Agrega un producto específico usando mensaje mockeado
    @PostMapping
    public ProductDTO addProduct() {
        KafkaMockService.AddProductMessage msg = kafkaMockService.getAddProductMock();
        ProductDTO dto = msg.payload.product;
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setMediaSrc(dto.getMediaSrc() != null ? dto.getMediaSrc() : List.of());
        product.setNew(dto.getIsNew() != null ? dto.getIsNew() : false);
        product.setBestseller(dto.getIsBestseller() != null ? dto.getIsBestseller() : false);
        product.setFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
        product.setHero(dto.getHero() != null ? dto.getHero() : false);
        product.setActive(dto.getActive() != null ? dto.getActive() : true);
        product.setDiscount(dto.getDiscount());
        product.setPriceUnit(dto.getPriceUnit());
        product.setProductCode(dto.getProductCode());
        product.setBrand(dto.getBrand() != null ? brandRepository.findById((long) dto.getBrand().getId().intValue()).orElse(null) : null);
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            Set<Category> cats = dto.getCategories().stream()
                .map(catDto -> categoryRepository.findById((long) catDto.getId().intValue()).orElse(null))
                .filter(c -> c != null)
                .collect(java.util.stream.Collectors.toSet());
            product.setCategories(new java.util.HashSet<>(cats));
        } else {
            product.setCategories(null);
        }
        product.setCalification(dto.getCalification() != null ? dto.getCalification() : 0f);
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    // Edita solo precio y stock usando mensaje mockeado
    public static class EditProductSimpleRequest {
        public Long id;
    }

    @PatchMapping("/simple")
    public ProductDTO editProductSimple() {
        KafkaMockService.EditProductSimpleMessage msg = kafkaMockService.getEditProductMockSimple();
        KafkaMockService.EditProductSimplePayload dto = msg.payload;
        Long id = dto.id;
        Optional<Product> productOpt = productRepository.findById(id.intValue());
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        if (dto.price != null) {
            product.setPrice(dto.price);
            Float discount = product.getDiscount() != null ? product.getDiscount() : 0f;
            Float priceUnit = dto.price / (1 - (discount / 100f));
            product.setPriceUnit(priceUnit);
        }
        if (dto.stock != null) product.setStock(dto.stock);
        Product updated = productRepository.save(product);
        return toDTO(updated);
    }

    // Edita el producto completo usando mensaje mockeado
    @PatchMapping
    public ProductDTO editProduct() {
        KafkaMockService.EditProductFullMessage msg = kafkaMockService.getEditProductMockFull();
        ProductDTO dto = msg.payload;
        Integer id = Math.toIntExact(dto.getId());
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        // Campos simples
        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        // MediaSrc: si es null, setear lista vacía
        if (dto.getMediaSrc() == null) {
            product.setMediaSrc(List.of());
        } else {
            product.setMediaSrc(new java.util.ArrayList<>(dto.getMediaSrc()));
        }
        // Booleanos: solo asignar si no son null
        if (dto.getIsNew() != null) product.setNew(dto.getIsNew());
        if (dto.getIsBestseller() != null) product.setBestseller(dto.getIsBestseller());
        if (dto.getIsFeatured() != null) product.setIsFeatured(dto.getIsFeatured());
        if (dto.getHero() != null) product.setHero(dto.getHero());
        // Active
        if (dto.getActive() != null) product.setActive(dto.getActive());
        // ProductCode
        if (dto.getProductCode() == null) {
            product.setProductCode(null);
        } else {
            product.setProductCode(dto.getProductCode());
        }
        // PriceUnit y Discount
        if (dto.getPriceUnit() != null) product.setPriceUnit(dto.getPriceUnit());
        if (dto.getDiscount() != null) product.setDiscount(dto.getDiscount());
        // Recalcular price si priceUnit o discount se tocan y ambos existen
        Float priceUnit = product.getPriceUnit();
        Float discount = product.getDiscount();
        if ((dto.getPriceUnit() != null || dto.getDiscount() != null) && priceUnit != null && discount != null) {
            Float price = priceUnit - (priceUnit * (discount / 100f));
            product.setPrice(price);
        } else if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        // Marca
        if (dto.getBrand() != null && dto.getBrand().getId() != null) {
            product.setBrand(brandRepository.findById((long) dto.getBrand().getId().intValue()).orElse(null));
        } else {
            product.setBrand(null);
        }
        // Categorías
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            Set<ar.edu.uade.ecommerce.Entity.Category> cats = dto.getCategories().stream()
                .map(catDto -> categoryRepository.findById((long) catDto.getId().intValue()).orElse(null))
                .filter(c -> c != null)
                .collect(java.util.stream.Collectors.toSet());
            product.setCategories(new java.util.HashSet<>(cats));
        } else {
            product.setCategories(null);
        }
        // Calificación
        product.setCalification(dto.getCalification() != null ? dto.getCalification() : 0f);
        Product updated = productRepository.save(product);
        return toDTO(updated);
    }

    // Activar producto usando mensaje mockeado
    @PatchMapping("/activate")
    public ProductDTO activateProduct() {
        KafkaMockService.ActivateProductMessage msg = kafkaMockService.getActivateProductMock();
        Long id = msg.payload.id;
        Optional<Product> productOpt = productRepository.findById(id.intValue());
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        product.setActive(true);
        Product updated = productRepository.save(product);
        return toDTO(updated);
    }

    // Desactivar producto usando mensaje mockeado
    @PatchMapping("/deactivate")
    public ProductDTO deactivateProduct() {
        KafkaMockService.DeactivateProductMessage msg = kafkaMockService.getDeactivateProductMock();
        Long id = msg.payload.id;
        Optional<Product> productOpt = productRepository.findById(id.intValue());
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        product.setActive(false);
        Product updated = productRepository.save(product);
        return toDTO(updated);
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
    @PostMapping("/{id}/review")
    public ReviewResponse addReview(@PathVariable Integer id, @RequestBody ReviewRequest reviewRequest) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        Review review = new Review();
        review.setProduct(product);
        review.setCalification(reviewRequest.getCalification());
        review.setDescription(reviewRequest.getDescription());
        reviewRepository.save(review);
        List<Review> reviews = reviewRepository.findByProduct(product);
        float promedio = (float) reviews.stream().mapToDouble(Review::getCalification).average().orElse(0.0);
        // Actualizar el campo calification en Product
        product.setCalification(promedio);
        productRepository.save(product);
        // Enviar evento simulado por Kafka
        kafkaMockService.sendEvent(new ar.edu.uade.ecommerce.Entity.Event(
            "PRODUCT_REVIEW_ADDED",
            String.format("Producto %d (%s) recibió una nueva review. Promedio actualizado: %.2f", product.getId(), product.getTitle(), promedio)
        ));
        List<ReviewDTO> reviewDTOs = reviews.stream()
            .map(r -> new ReviewDTO(r.getId(), r.getCalification(), r.getDescription()))
            .collect(Collectors.toList());
        return new ReviewResponse(product.getId(), product.getTitle(), promedio, reviewDTOs);
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
    public List<ProductDTO> filterProducts(@RequestBody FilterProductRequest filterRequest) {
        List<Product> products = productRepository.findAll();
        Long categoryId = filterRequest.getCategoryId();
        Long brandId = filterRequest.getBrandId();
        Float priceMin = filterRequest.getPriceMin();
        Float priceMax = filterRequest.getPriceMax();
        return products.stream()
            .filter(p -> {
                boolean matches = true;
                if (categoryId != null && (p.getCategories() == null || p.getCategories().stream().noneMatch(c -> c.getId().equals(categoryId.intValue())))) {
                    matches = false;
                }
                if (brandId != null && (p.getBrand() == null || !p.getBrand().getId().equals(brandId.intValue()))) {
                    matches = false;
                }
                if (priceMin != null && (p.getPrice() == null || p.getPrice() < priceMin)) {
                    matches = false;
                }
                if (priceMax != null && (p.getPrice() == null || p.getPrice() > priceMax)) {
                    matches = false;
                }
                return matches;
            })
            .map(this::toDTO)
            .collect(Collectors.toList());
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
        // Enviar evento mock
        kafkaMockService.sendEvent(new ar.edu.uade.ecommerce.Entity.Event(
            "ADD_FAVOURITE_PRODUCT",
            String.format("{productCode: '%s', id: %d, nombre: '%s'}", productCode, product.getId(), product.getTitle())
        ));
        return "El producto con código " + productCode + " se agregó a favoritos correctamente.";
    }

    // Quita un producto de favoritos
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
        // Enviar evento mock
        kafkaMockService.sendEvent(new ar.edu.uade.ecommerce.Entity.Event(
            "REMOVE_FAVOURITE_PRODUCT",
            String.format("{productCode: '%s', id: %d, nombre: '%s'}", productCode, product.getId(), product.getTitle())
        ));
        return "El producto con código " + productCode + " ya no es favorito.";
    }
}
