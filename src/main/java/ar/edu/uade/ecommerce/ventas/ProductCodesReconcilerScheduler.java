package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Repository.BrandRepository;
import ar.edu.uade.ecommerce.Repository.CategoryRepository;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

@Component
@ConditionalOnProperty(prefix = "reconcile.codes", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ProductCodesReconcilerScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProductCodesReconcilerScheduler.class);

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Value("${reconcile.codes.placeholder.brand:UNBRANDED}")
    private String placeholderBrandName;

    @Value("${reconcile.codes.placeholder.category:UNCATEGORIZED}")
    private String placeholderCategoryName;

    public ProductCodesReconcilerScheduler(ProductRepository productRepository,
                                           BrandRepository brandRepository,
                                           CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

    // Cada 8 horas por defecto
    @Scheduled(cron = "${reconcile.codes.cron:0 0 */8 * * *}")
    @Transactional
    public void reconcile() {
        List<Product> all = productRepository.findAll();
        AtomicInteger fixedBrands = new AtomicInteger();
        AtomicInteger fixedCats = new AtomicInteger();
        AtomicInteger activated = new AtomicInteger();
        AtomicInteger deactivated = new AtomicInteger();

        // Prefetch placeholders (crear si no existen)
        Brand placeholderBrand = ensurePlaceholderBrand();
        Category placeholderCategory = ensurePlaceholderCategory();

        for (Product p : all) {
            boolean changed = false;

            // Brand
            Brand b = p.getBrand();
            if (b == null) {
                // Asignar placeholder inactivo
                p.setBrand(placeholderBrand);
                changed = true;
                fixedBrands.incrementAndGet();
            } else {
                if (b.getBrandCode() == null) {
                    Integer code = generateDeterministicCode(b.getName());
                    b.setBrandCode(code);
                    brandRepository.save(b);
                    fixedBrands.incrementAndGet();
                }
            }

            // Categories
            Set<Category> cats = p.getCategories();
            if (cats == null || cats.isEmpty()) {
                // Vincular placeholder inactivo
                Set<Category> newSet = new HashSet<>();
                newSet.add(placeholderCategory);
                p.setCategories(newSet);
                changed = true;
                fixedCats.incrementAndGet();
            } else {
                for (Category c : cats) {
                    if (c.getCategoryCode() == null) {
                        Integer code = generateDeterministicCode(c.getName());
                        c.setCategoryCode(code);
                        categoryRepository.save(c);
                        fixedCats.incrementAndGet();
                    }
                }
            }

            // Activación del producto según regla:
            // activo solo si tiene brand con code y al menos 1 categoría con code (todas con code)
            boolean readyBrand = p.getBrand() != null && p.getBrand().getBrandCode() != null;
            boolean readyCats = p.getCategories() != null && !p.getCategories().isEmpty() &&
                    p.getCategories().stream().allMatch(c -> c.getCategoryCode() != null);
            Boolean currentActive = p.getActive();
            boolean shouldBeActive = readyBrand && readyCats;
            if (!shouldBeActive) {
                if (currentActive == null || currentActive) {
                    p.setActive(false);
                    changed = true;
                    deactivated.incrementAndGet();
                }
            } else {
                if (currentActive == null || !currentActive) {
                    p.setActive(true);
                    changed = true;
                    activated.incrementAndGet();
                }
            }

            if (changed) {
                productRepository.save(p);
            }
        }

        log.info("[ReconcileCodes] Brands fixed={} | Categories fixed={} | Activated={} | Deactivated={}",
                fixedBrands.get(), fixedCats.get(), activated.get(), deactivated.get());
    }

    private Brand ensurePlaceholderBrand() {
        Brand b = brandRepository.findByName(placeholderBrandName).orElse(null);
        if (b == null) {
            b = new Brand();
            b.setName(placeholderBrandName);
            b.setActive(false);
            b.setBrandCode(generateDeterministicCode(placeholderBrandName));
            b = brandRepository.save(b);
        } else {
            if (b.getBrandCode() == null) {
                b.setBrandCode(generateDeterministicCode(placeholderBrandName));
                b = brandRepository.save(b);
            }
            // asegurar inactivo
            if (b.isActive()) {
                b.setActive(false);
                b = brandRepository.save(b);
            }
        }
        return b;
    }

    private Category ensurePlaceholderCategory() {
        Category c = categoryRepository.findByName(placeholderCategoryName).orElse(null);
        if (c == null) {
            c = new Category();
            c.setName(placeholderCategoryName);
            c.setActive(false);
            c.setCategoryCode(generateDeterministicCode(placeholderCategoryName));
            c = categoryRepository.save(c);
        } else {
            if (c.getCategoryCode() == null) {
                c.setCategoryCode(generateDeterministicCode(placeholderCategoryName));
                c = categoryRepository.save(c);
            }
            // asegurar inactivo
            if (c.isActive()) {
                c.setActive(false);
                c = categoryRepository.save(c);
            }
        }
        return c;
    }

    private Integer generateDeterministicCode(String name) {
        String base = (name == null ? "NA" : name.trim().toLowerCase(Locale.ROOT));
        CRC32 crc = new CRC32();
        crc.update(base.getBytes(StandardCharsets.UTF_8));
        long raw = crc.getValue();
        int code = (int) (10000 + (raw % 990000));
        return code;
    }
}

