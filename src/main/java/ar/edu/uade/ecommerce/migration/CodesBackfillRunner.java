package ar.edu.uade.ecommerce.migration;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Repository.BrandRepository;
import ar.edu.uade.ecommerce.Repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.CRC32;

@Component
public class CodesBackfillRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CodesBackfillRunner.class);

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final Environment env;

    public CodesBackfillRunner(BrandRepository brandRepository,
                               CategoryRepository categoryRepository,
                               Environment env) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.env = env;
    }

    @Override
    public void run(String... args) {
        boolean enabled = env.getProperty("codes.backfill.enabled", Boolean.class, true);
        if (!enabled) {
            log.info("[Backfill] Deshabilitado por propiedad codes.backfill.enabled=false");
            return;
        }
        try {
            backfillBrands();
            backfillCategories();
        } catch (Exception ex) {
            log.warn("[Backfill] Error realizando backfill de códigos: {}", ex.getMessage());
        }
    }

    private void backfillBrands() {
        List<Brand> all = brandRepository.findAll();
        Set<Integer> used = new HashSet<>();
        all.stream().map(Brand::getBrandCode).filter(c -> c != null).forEach(used::add);
        int changed = 0;
        for (Brand b : all) {
            if (b.getBrandCode() == null) {
                Integer code = generateDeterministicCode(b.getName(), used);
                b.setBrandCode(code);
                brandRepository.save(b);
                used.add(code);
                changed++;
            }
        }
        if (changed > 0) log.info("[Backfill] Brand codes asignados: {}", changed);
    }

    private void backfillCategories() {
        List<Category> all = categoryRepository.findAll();
        Set<Integer> used = new HashSet<>();
        all.stream().map(Category::getCategoryCode).filter(c -> c != null).forEach(used::add);
        int changed = 0;
        for (Category c : all) {
            if (c.getCategoryCode() == null) {
                Integer code = generateDeterministicCode(c.getName(), used);
                c.setCategoryCode(code);
                categoryRepository.save(c);
                used.add(code);
                changed++;
            }
        }
        if (changed > 0) log.info("[Backfill] Category codes asignados: {}", changed);
    }

    private Integer generateDeterministicCode(String name, Set<Integer> used) {
        String base = (name == null ? "NA" : name.trim().toLowerCase(Locale.ROOT));
        CRC32 crc = new CRC32();
        crc.update(base.getBytes(StandardCharsets.UTF_8));
        long raw = crc.getValue();
        // Mapear a rango visible 10000..999999
        int code = (int) (10000 + (raw % 990000));
        // Evitar colisiones
        while (used.contains(code)) {
            code = code + 1; // simple step forward; en práctica colisiones serán raras
            if (code > 999999) code = 10000;
        }
        return code;
    }
}

