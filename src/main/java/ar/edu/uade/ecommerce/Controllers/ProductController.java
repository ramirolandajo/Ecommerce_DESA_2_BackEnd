package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.DTO.BrandDTO;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Entity.DTO.ProductDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Service.ProductService;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import ar.edu.uade.ecommerce.Entity.Review;
import ar.edu.uade.ecommerce.Repository.ReviewRepository;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
    @PersistenceContext
    EntityManager entityManager;

    // Sincroniza productos desde el mock
    @GetMapping("/sync")
    public List<ProductDTO> syncProductsFromMock() {
        List<ProductDTO> mockProducts = kafkaMockService.getProductsMock();
        productRepository.deleteAll();
        for (ProductDTO dto : mockProducts) {
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
            // Relacionar Brand y Category usando EntityManager por id
            if (dto.getBrand() != null && dto.getBrand().getId() != null) {
                Brand brand = entityManager.find(Brand.class, dto.getBrand().getId().intValue());
                if (brand != null) {
                    product.setBrand(brand);
                } else {
                    throw new RuntimeException("La marca con id " + dto.getBrand().getId() + " no existe");
                }
            }
            if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
                Set<Category> categories = dto.getCategories().stream()
                        .map(catDto -> catDto.getId() != null ? entityManager.find(Category.class, catDto.getId().intValue()) : null)
                        .filter(c -> c != null)
                        .collect(Collectors.toSet());
                product.setCategories(categories);
            }
            productRepository.save(product);
        }
        return productRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // Obtiene todos los productos
    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // Agrega un producto específico
    @PostMapping
    public ProductDTO addProduct(@RequestBody ProductDTO dto) {
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
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    // Edita solo precio y stock
    @PatchMapping("/{id}/simple")
    public ProductDTO editProductSimple(@PathVariable Long id, @RequestBody ProductDTO dto) {
        Optional<Product> productOpt = productRepository.findById(id.intValue());
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
            Float discount = product.getDiscount() != null ? product.getDiscount() : 0f;
            // Ingeniería inversa para recalcular priceUnit
            Float priceUnit = dto.getPrice() / (1 - (discount / 100f));
            product.setPriceUnit(priceUnit);
        }
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getDiscount() != null) {
            product.setDiscount(dto.getDiscount());
            // Si se toca el descuento, recalcular price
            Float priceUnit = product.getPriceUnit() != null ? product.getPriceUnit() : null;
            Float discount = dto.getDiscount();
            if (priceUnit != null) {
                Float price = priceUnit - (priceUnit * (discount / 100f));
                product.setPrice(price);
            }
        }
        Product updated = productRepository.save(product);
        return toDTO(updated);
    }

    // Edita el producto completo
    @PatchMapping("/{id}")
    public ProductDTO editProduct(@PathVariable Integer id, @RequestBody ProductDTO dto) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setStock(dto.getStock());
        product.setMediaSrc(dto.getMediaSrc() != null ? dto.getMediaSrc() : List.of());
        product.setNew(dto.getIsNew() != null ? dto.getIsNew() : false);
        product.setBestseller(dto.getIsBestseller() != null ? dto.getIsBestseller() : false);
        product.setFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
        product.setHero(dto.getHero() != null ? dto.getHero() : false);
        product.setActive(dto.getActive() != null ? dto.getActive() : true);
        product.setProductCode(dto.getProductCode());
        // Lógica de precio unitario y descuento
        if (dto.getPriceUnit() != null) product.setPriceUnit(dto.getPriceUnit());
        if (dto.getDiscount() != null) product.setDiscount(dto.getDiscount());
        // Si se tocó priceUnit o discount, recalcular price
        if (dto.getPriceUnit() != null || dto.getDiscount() != null) {
            Float priceUnit = product.getPriceUnit();
            Float discount = product.getDiscount() != null ? product.getDiscount() : 0f;
            if (priceUnit != null) {
                Float price = priceUnit - (priceUnit * (discount / 100f));
                product.setPrice(price);
            }
        } else if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        Product updated = productRepository.save(product);
        return toDTO(updated);
    }

    // Activar producto
    @PatchMapping("/{id}/activate")
    public ProductDTO activateProduct(@PathVariable Integer id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        product.setActive(true); // ejemplo de campo para activar
        Product updated = productRepository.save(product);
        return toDTO(updated);
    }

    // Desactivar producto
    @PatchMapping("/{id}/deactivate")
    public ProductDTO deactivateProduct(@PathVariable Integer id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Product product = productOpt.get();
        product.setActive(false); // ejemplo de campo para desactivar
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

    // Conversión a DTO
    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(Long.valueOf(product.getId()));
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setMediaSrc(product.getMediaSrc());
        dto.setCalification(product.getCalification());
        dto.setDiscount(product.getDiscount());
        dto.setPriceUnit(product.getPriceUnit());
        dto.setProductCode(product.getProductCode());
        dto.setActive(product.getActive());
        // Devuelve el nombre de la marca y categorías
        if (product.getBrand() != null) {
            dto.setBrand(new BrandDTO(Long.valueOf(product.getBrand().getId()), product.getBrand().getName(), product.getBrand().isActive()));
        }
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            dto.setCategories(product.getCategories().stream()
                .map(cat -> new CategoryDTO(Long.valueOf(cat.getId()), cat.getName(), cat.isActive()))
                .collect(Collectors.toList()));
        }
        dto.setIsNew(product.getIsNew());
        dto.setIsBestseller(product.isIsBestseller());
        dto.setIsFeatured(product.isIsFeatured());
        dto.setHero(product.isHero());
        return dto;
    }
}
