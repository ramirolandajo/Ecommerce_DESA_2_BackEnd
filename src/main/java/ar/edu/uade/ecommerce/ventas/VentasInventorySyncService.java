package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Repository.BrandRepository;
import ar.edu.uade.ecommerce.Repository.CategoryRepository;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VentasInventorySyncService {
    private static final Logger log = LoggerFactory.getLogger(VentasInventorySyncService.class);

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    private final ObjectMapper mapper;

    public VentasInventorySyncService(ProductRepository productRepository,
                                      BrandRepository brandRepository,
                                      CategoryRepository categoryRepository,
                                      ObjectMapper mapper) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Transactional
    public void actualizarStock(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        Integer productCode = asInt(p, "productCode");
        Integer stock = asInt(p, "stock");
        if (productCode == null || stock == null) {
            throw new IllegalArgumentException("Payload inválido para Actualizar stock: productCode/stock faltan");
        }
        Product prod = productRepository.findByProductCode(productCode);
        if (prod == null) {
            log.info("[Inventario->Ventas] Producto inexistente, se crea placeholder para productCode={}", productCode);
            prod = new Product();
            prod.setProductCode(productCode);
            prod.setActive(true);
            prod.setTitle(asText(p, "name", "nombre", "title"));
        }
        prod.setStock(stock);
        productRepository.save(prod);
    }

    @Transactional
    public void crearProducto(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        Integer productCode = asInt(p, "productCode");
        if (productCode == null) throw new IllegalArgumentException("Payload sin productCode en crearProducto");
        Product prod = Optional.ofNullable(productRepository.findByProductCode(productCode)).orElse(new Product());
        applyFullProduct(prod, p);
        productRepository.save(prod);
        log.info("[Inventario->Ventas] Producto creado/actualizado productCode={} id={} active={}", prod.getProductCode(), prod.getId(), prod.getActive());
    }

    @Transactional
    public void modificarProducto(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        Integer productCode = asInt(p, "productCode");
        if (productCode == null) throw new IllegalArgumentException("Payload sin productCode en modificarProducto");
        Product prod = Optional.ofNullable(productRepository.findByProductCode(productCode))
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado para productCode=" + productCode));
        applyPartialProduct(prod, p);
        productRepository.save(prod);
        log.info("[Inventario->Ventas] Producto modificado productCode={} id={} active={}", prod.getProductCode(), prod.getId(), prod.getActive());
    }

    @Transactional
    public void desactivarProducto(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        Integer productCode = asInt(p, "productCode");
        Product prod = resolveByProductCodeOrId(p, productCode);
        prod.setActive(false);
        productRepository.save(prod);
        log.info("[Inventario->Ventas] Producto DESACTIVADO productCode={} id={} active={}", prod.getProductCode(), prod.getId(), prod.getActive());
    }

    @Transactional
    public void activarProducto(Object payload) {
        log.info("[Inventario->Ventas] Activando producto con payload: {}", payload);
        JsonNode p = mapper.valueToTree(payload);
        Integer productCode = asInt(p, "productCode");
        Product prod = resolveByProductCodeOrId(p, productCode);
        prod.setActive(true);
        productRepository.save(prod);
        log.info("[Inventario->Ventas] Producto ACTIVADO productCode={} id={} active={}", prod.getProductCode(), prod.getId(), prod.getActive());
    }

    @Transactional
    public void upsertProducto(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        Integer productCode = asInt(p, "productCode");
        if (productCode == null) throw new IllegalArgumentException("Payload sin productCode en upsertProducto");
        Product prod = Optional.ofNullable(productRepository.findByProductCode(productCode)).orElse(new Product());
        applyFullProduct(prod, p);
        productRepository.save(prod);
        log.info("[Inventario->Ventas] Producto upsert productCode={} id={} active={}", prod.getProductCode(), prod.getId(), prod.getActive());
    }

    @Transactional
    public void crearMarca(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        // Preferir brandCode si viene; fallback a nombre
        Integer brandCode = asInt(p, "brandCode", "brand_code", "brand");
        String name = asText(p, "brandName", "name", "nombre");
        Brand b = null;
        if (brandCode != null) b = brandRepository.findByBrandCode(brandCode).orElse(null);
        if (b == null && name != null) b = brandRepository.findByName(name).orElse(null);
        if (b == null) {
            b = new Brand();
            if (name != null) b.setName(name);
        }
        if (brandCode != null) b.setBrandCode(brandCode);
        if (name != null) b.setName(name);
        b.setActive(true);
        brandRepository.save(b);
        log.info("[Inventario->Ventas] Marca creada/actualizada brandCode={} name={} id={}", b.getBrandCode(), b.getName(), b.getId());
    }

    @Transactional
    public void crearCategoria(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        Integer categoryCode = asInt(p, "categoryCode", "category_code", "id");
        String name = asText(p, "name", "nombre");
        Category c = null;
        if (categoryCode != null) c = categoryRepository.findByCategoryCode(categoryCode).orElse(null);
        if (c == null && name != null) c = categoryRepository.findByName(name).orElse(null);
        if (c == null) {
            c = new Category();
            if (name != null) c.setName(name);
        }
        if (categoryCode != null) c.setCategoryCode(categoryCode);
        if (name != null) c.setName(name);
        c.setActive(true);
        categoryRepository.save(c);
        log.info("[Inventario->Ventas] Categoria creada/actualizada categoryCode={} name={} id={}", c.getCategoryCode(), c.getName(), c.getId());
    }

    @Transactional
    public void desactivarMarca(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        Integer brandCode = asInt(p, "brandCode", "brand_code", "brand");
        String name = asText(p, "name", "nombre", "brandName");
        Optional<Brand> ob = Optional.empty();
        if (brandCode != null) ob = brandRepository.findByBrandCode(brandCode);
        if (ob.isEmpty() && name != null) ob = brandRepository.findByName(name);
        ob.ifPresent(b -> { b.setActive(false); brandRepository.save(b); });
    }

    @Transactional
    public void desactivarCategoria(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        Integer categoryCode = asInt(p, "categoryCode", "category_code", "id");
        String name = asText(p, "name", "nombre");
        Optional<Category> oc = Optional.empty();
        if (categoryCode != null) oc = categoryRepository.findByCategoryCode(categoryCode);
        if (oc.isEmpty() && name != null) oc = categoryRepository.findByName(name);
        oc.ifPresent(c -> { c.setActive(false); categoryRepository.save(c); });
    }

    @Transactional
    public void activarMarca(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        Integer brandCode = asInt(p, "brandCode", "brand_code", "brand");
        String name = asText(p, "name", "nombre", "brandName");
        Optional<Brand> ob = Optional.empty();
        if (brandCode != null) ob = brandRepository.findByBrandCode(brandCode);
        if (ob.isEmpty() && name != null) ob = brandRepository.findByName(name);
        if (ob.isEmpty() && p.hasNonNull("id")) {
            int id = p.get("id").asInt();
            ob = brandRepository.findById(id);
        }
        if (ob.isEmpty()) {
            log.warn("[Inventario->Ventas] Activar marca: no encontrada (brandCode={}, name={})", brandCode, name);
            return;
        }
        Brand b = ob.get();
        b.setActive(true);
        brandRepository.save(b);
        log.info("[Inventario->Ventas] Marca ACTIVADA brandCode={} name={} id={} active={}", b.getBrandCode(), b.getName(), b.getId(), b.isActive());
    }

    @Transactional
    public void activarCategoria(Object payload) {
        JsonNode p = mapper.valueToTree(payload);
        Integer categoryCode = asInt(p, "categoryCode", "category_code", "id");
        String name = asText(p, "name", "nombre");
        Optional<Category> oc = Optional.empty();
        if (categoryCode != null) oc = categoryRepository.findByCategoryCode(categoryCode);
        if (oc.isEmpty() && name != null) oc = categoryRepository.findByName(name);
        if (oc.isEmpty() && p.hasNonNull("id")) {
            int id = p.get("id").asInt();
            oc = categoryRepository.findById(id);
        }
        if (oc.isEmpty()) {
            log.warn("[Inventario->Ventas] Activar categoría: no encontrada (categoryCode={}, name={})", categoryCode, name);
            return;
        }
        Category c = oc.get();
        c.setActive(true);
        categoryRepository.save(c);
        log.info("[Inventario->Ventas] Categoría ACTIVADA categoryCode={} name={} id={} active={}", c.getCategoryCode(), c.getName(), c.getId(), c.isActive());
    }

    @Transactional
    public void crearProductosBatch(Object payload) {
        JsonNode p;
        try {
            if (payload instanceof JsonNode) {
                p = (JsonNode) payload;
            } else if (payload instanceof String) {
                p = mapper.readTree((String) payload);
            } else if (payload instanceof byte[]) {
                p = mapper.readTree((byte[]) payload);
            } else {
                p = mapper.valueToTree(payload);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Payload inválido para batch: no se puede parsear", e);
        }

        JsonNode items = p != null && p.isArray() ? p : (p != null ? p.get("items") : null);
        if (items == null || !items.isArray()) {
            throw new IllegalArgumentException("Payload inválido para batch: falta array 'items' o el payload no es un array");
        }

        int ok = 0, fail = 0;
        for (JsonNode item : items) {
            try {
                Integer productCode = asInt(item, "productCode", "product_code");
                if (productCode == null) throw new IllegalArgumentException("Item sin productCode");
                Product prod = Optional.ofNullable(productRepository.findByProductCode(productCode)).orElse(new Product());
                applyFullProduct(prod, item);
                productRepository.save(prod);
                ok++;
            } catch (Exception ex) {
                fail++;
                log.warn("[Inventario->Ventas][Batch] Error procesando item: {}", ex.getMessage());
            }
        }
        log.info("[Inventario->Ventas][Batch] Procesados items OK={} FAIL={}", ok, fail);
    }

    // ================= Helpers =================

    private Product resolveByProductCodeOrId(JsonNode p, Integer productCode) {
        Product prod = null;
        if (productCode != null) prod = productRepository.findByProductCode(productCode);
        if (prod == null && p.hasNonNull("id")) {
            int id = p.get("id").asInt();
            prod = productRepository.findById(id).orElse(null);
        }
        if (prod == null && productCode != null) {
            prod = new Product();
            prod.setProductCode(productCode);
        }
        if (prod == null) throw new NoSuchElementException("Producto no encontrado en payload: " + p);
        return prod;
    }

    private void applyFullProduct(Product prod, JsonNode p) {
        Integer productCode = asInt(p, "productCode");
        if (productCode != null) prod.setProductCode(productCode);

        prod.setTitle(asText(p, "name", "nombre", "title"));
        prod.setDescription(asText(p, "description"));

        // unit price admite unitPrice o unit_price
        if (hasAny(p, "unitPrice", "unit_price")) prod.setPriceUnit(asFloat(p, "unitPrice", "unit_price"));
        if (hasAny(p, "price")) prod.setPrice(asFloat(p, "price"));
        if (hasAny(p, "discount")) prod.setDiscount(asFloat(p, "discount"));
        if (hasAny(p, "stock")) prod.setStock(asInt(p, "stock"));
        if (hasAny(p, "new", "isNew", "is_new")) prod.setNew(Boolean.TRUE.equals(asBool(p, "new", "isNew", "is_new")));
        if (hasAny(p, "bestSeller", "isBestseller", "is_best_seller")) prod.setBestseller(Boolean.TRUE.equals(asBool(p, "bestSeller", "isBestseller", "is_best_seller")));
        if (hasAny(p, "featured", "isFeatured", "is_featured")) prod.setFeatured(Boolean.TRUE.equals(asBool(p, "featured", "isFeatured", "is_featured")));
        if (hasAny(p, "hero")) prod.setHero(Boolean.TRUE.equals(asBool(p, "hero")));
        if (hasAny(p, "active")) prod.setActive(Boolean.TRUE.equals(asBool(p, "active")));
        if (hasAny(p, "calification", "rating")) prod.setCalification(asFloat(p, "calification", "rating"));

        // Media
        if (p.has("images") && p.get("images").isArray()) {
            List<String> imgs = new ArrayList<>();
            p.get("images").forEach(n -> imgs.add(n.asText()));
            prod.setMediaSrc(imgs);
        }

        // Brand por código primero; fallback brandId
        Integer brandCode = asInt(p, "brandCode", "brand_code");
        if (brandCode == null && p.has("brand") && p.get("brand").isInt()) brandCode = p.get("brand").asInt();
        String brandName = asText(p, "brandName");
        Brand b = null;
        if (brandCode != null) b = brandRepository.findByBrandCode(brandCode).orElse(null);
        if (b == null && brandName != null) b = brandRepository.findByName(brandName).orElse(null);
        if (b == null && p.hasNonNull("brandId")) {
            int bid = p.get("brandId").asInt();
            b = brandRepository.findById(bid).orElse(null);
        }
        if (b == null && (brandCode != null || brandName != null)) {
            b = new Brand();
            b.setActive(true);
            if (brandName != null) b.setName(brandName); else b.setName("BRAND-" + (brandCode != null ? brandCode : "NA"));
            if (brandCode != null) b.setBrandCode(brandCode);
            b = brandRepository.save(b);
        }
        if (b != null) prod.setBrand(b);

        // Categories por códigos primero (categories/categoryCodes) + fallback categoryIds
        Set<Category> catSet = resolveCategoriesFromPayload(p);
        if (!catSet.isEmpty()) {
            prod.setCategories(catSet);
        }
    }

    private void applyPartialProduct(Product prod, JsonNode p) {
        if (hasAny(p, "name", "nombre", "title")) prod.setTitle(asText(p, "name", "nombre", "title"));
        if (hasAny(p, "description")) prod.setDescription(asText(p, "description"));
        // unit price admite unitPrice o unit_price
        if (hasAny(p, "unitPrice", "unit_price")) prod.setPriceUnit(asFloat(p, "unitPrice", "unit_price"));
        if (hasAny(p, "price")) prod.setPrice(asFloat(p, "price"));
        if (hasAny(p, "discount")) prod.setDiscount(asFloat(p, "discount"));
        if (hasAny(p, "stock")) prod.setStock(asInt(p, "stock"));
        if (hasAny(p, "new", "isNew")) prod.setNew(Boolean.TRUE.equals(asBool(p, "new", "isNew")));
        if (hasAny(p, "bestSeller", "isBestseller")) prod.setBestseller(Boolean.TRUE.equals(asBool(p, "bestSeller", "isBestseller")));
        if (hasAny(p, "featured", "isFeatured")) prod.setFeatured(Boolean.TRUE.equals(asBool(p, "featured", "isFeatured")));
        if (hasAny(p, "hero")) prod.setHero(Boolean.TRUE.equals(asBool(p, "hero")));
        if (hasAny(p, "active")) prod.setActive(Boolean.TRUE.equals(asBool(p, "active")));
        if (hasAny(p, "calification", "rating")) prod.setCalification(asFloat(p, "calification", "rating"));
        if (hasAny(p, "images")) {
            if (p.has("images") && p.get("images").isArray()) {
                List<String> imgs = new ArrayList<>();
                p.get("images").forEach(n -> imgs.add(n.asText()));
                prod.setMediaSrc(imgs);
            }
        }

        // Brand update con fallback brandId
        Integer brandCode = asInt(p, "brandCode", "brand_code");
        if (brandCode == null && p.has("brand") && p.get("brand").isInt()) brandCode = p.get("brand").asInt();
        String brandName = asText(p, "brandName");
        Brand b = null;
        if (brandCode != null) b = brandRepository.findByBrandCode(brandCode).orElse(null);
        if (b == null && brandName != null) b = brandRepository.findByName(brandName).orElse(null);
        if (b == null && p.hasNonNull("brandId")) {
            int bid = p.get("brandId").asInt();
            b = brandRepository.findById(bid).orElse(null);
        }
        if (b == null && (brandCode != null || brandName != null)) {
            b = new Brand();
            b.setActive(true);
            if (brandName != null) b.setName(brandName); else b.setName("BRAND-" + (brandCode != null ? brandCode : "NA"));
            if (brandCode != null) b.setBrandCode(brandCode);
            b = brandRepository.save(b);
        }
        if (b != null) prod.setBrand(b);

        // Categories update con categoryIds también
        Set<Category> catSet = resolveCategoriesFromPayload(p);
        if (!catSet.isEmpty()) {
            prod.setCategories(catSet);
        }
    }

    private Set<Category> resolveCategoriesFromPayload(JsonNode p) {
        List<Integer> catCodes = asIntList(p, "categoryCodes", "categories");
        List<Integer> catIds = asIntList(p, "categoryIds");
        Set<Category> catSet = new HashSet<>();
        if (catCodes != null && !catCodes.isEmpty()) {
            for (Integer code : catCodes) {
                if (code == null) continue;
                Category c = categoryRepository.findByCategoryCode(code).orElseGet(() -> {
                    // fallback: intentar por ID interna
                    Optional<Category> oc = categoryRepository.findById(code);
                    if (oc.isPresent()) return oc.get();
                    Category nc = new Category();
                    nc.setActive(true);
                    nc.setCategoryCode(code);
                    nc.setName("CAT-" + code);
                    return categoryRepository.save(nc);
                });
                catSet.add(c);
            }
        }
        if (catIds != null && !catIds.isEmpty()) {
            for (Integer id : catIds) {
                if (id == null) continue;
                categoryRepository.findById(id).ifPresent(catSet::add);
            }
        }
        return catSet;
    }

    // ======= JSON helpers tolerantes =======
    private boolean hasAny(JsonNode n, String... keys) {
        for (String k : keys) if (n.has(k) && !n.get(k).isNull()) return true;
        return false;
    }

    private String asText(JsonNode n, String... keys) {
        for (String k : keys) if (n.hasNonNull(k)) return n.get(k).asText();
        return null;
    }

    private Integer asInt(JsonNode n, String... keys) {
        for (String k : keys) if (n.hasNonNull(k)) {
            if (n.get(k).isInt()) return n.get(k).asInt();
            try { return Integer.parseInt(n.get(k).asText()); } catch (Exception ignored) {}
        }
        return null;
    }

    private Float asFloat(JsonNode n, String... keys) {
        for (String k : keys) if (n.hasNonNull(k)) {
            if (n.get(k).isNumber()) return n.get(k).floatValue();
            try { return Float.parseFloat(n.get(k).asText()); } catch (Exception ignored) {}
        }
        return null;
    }

    private Boolean asBool(JsonNode n, String... keys) {
        for (String k : keys) if (n.hasNonNull(k)) {
            if (n.get(k).isBoolean()) return n.get(k).asBoolean();
            try { return Boolean.parseBoolean(n.get(k).asText()); } catch (Exception ignored) {}
        }
        return null;
    }

    private List<Integer> asIntList(JsonNode n, String... keys) {
        for (String k : keys) if (n.has(k) && n.get(k).isArray()) {
            List<Integer> out = new ArrayList<>();
            n.get(k).forEach(node -> {
                if (node.isInt()) out.add(node.asInt());
                else {
                    try { out.add(Integer.parseInt(node.asText())); } catch (Exception ignored) {}
                }
            });
            return out;
        }
        return Collections.emptyList();
    }
}
