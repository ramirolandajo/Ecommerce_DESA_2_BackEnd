package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private KafkaMockService kafkaMockService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/sync")
    public List<CategoryDTO> syncCategoriesFromMock() {
        KafkaMockService.CategorySyncMessage message = kafkaMockService.getCategoriesMock();
        List<CategoryDTO> mockCategories = message.payload.categories;
        List<Category> existingCategories = categoryService.getAllCategories();
        for (CategoryDTO dto : mockCategories) {
            Category existing = existingCategories.stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(dto.getName()))
                .findFirst()
                .orElse(null);
            if (existing == null) {
                Category c = new Category();
                c.setName(dto.getName());
                c.setActive(dto.getActive() != null ? dto.getActive() : true);
                categoryService.saveCategory(c);
            }
        }
        // Imprimir el mensaje recibido del mock en formato core de mensajería
        System.out.println("Mensaje recibido del core de mensajería:");
        System.out.println("{" +
            "type='" + message.type + "', " +
            "payload=" + message.payload + ", " +
            "timestamp=" + message.timestamp +
            "}");
        return categoryService.getAllCategories().stream()
                .map(c -> new CategoryDTO(Long.valueOf(c.getId()), c.getName(), c.isActive()))
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategories().stream()
                .map(c -> new CategoryDTO(Long.valueOf(c.getId()), c.getName(), c.isActive()))
                .collect(Collectors.toList());
    }

    @PostMapping
    public CategoryDTO addCategory(@RequestBody CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setActive(categoryDTO.getActive() != null ? categoryDTO.getActive() : true);
        Category saved = categoryService.saveCategory(category);
        return new CategoryDTO(Long.valueOf(saved.getId()), saved.getName(), saved.isActive());
    }

    @PatchMapping("/{id}")
    public CategoryDTO updateCategory(@PathVariable Integer id, @RequestBody CategoryDTO categoryDTO) {
        Category category = categoryService.getAllCategories().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        // Solo actualiza si el valor recibido NO es null
        if (categoryDTO.getName() != null) {
            category.setName(categoryDTO.getName());
        }
        if (categoryDTO.getActive() != null) {
            category.setActive(categoryDTO.getActive());
        }
        Category updated = categoryService.saveCategory(category);
        // El DTO de respuesta nunca debe tener active en null, siempre devuelve el valor actual de la entidad
        String responseName = categoryDTO.getName() == null ? null : updated.getName();
        Boolean responseActive = updated.isActive();
        return new CategoryDTO(Long.valueOf(updated.getId()), responseName, responseActive);
    }

    @DeleteMapping("/{id}")
    public List<CategoryDTO> deleteCategory(@PathVariable Integer id) {
        Category category = categoryService.getAllCategories().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        category.setActive(false);
        categoryService.saveCategory(category);
        // Devuelve el listado completo con el campo active
        return categoryService.getAllCategories().stream()
                .map(c -> new CategoryDTO(Long.valueOf(c.getId()), c.getName(), c.isActive()))
                .collect(java.util.stream.Collectors.toList());
    }

    @PostMapping("/bulk")
    public List<CategoryDTO> addBulkCategories(@RequestBody List<CategoryDTO> categoryDTOs) {
        if (categoryDTOs == null || categoryDTOs.isEmpty()) {
            return List.of();
        }
        List<Category> existingCategories = categoryService.getAllCategories() != null ? new java.util.ArrayList<>(categoryService.getAllCategories()) : new java.util.ArrayList<>();
        List<CategoryDTO> resultCategories = new java.util.ArrayList<>();
        List<String> processedNames = new java.util.ArrayList<>();
        boolean nullNameAdded = false;
        boolean nullNameExists = existingCategories.stream().anyMatch(c -> c.getName() == null);
        for (CategoryDTO dto : categoryDTOs) {
            if (dto == null) {
                continue;
            }
            if (dto.getName() == null) {
                // Solo agregar una categoría con nombre null por petición y si no existe en la base
                if (nullNameAdded || nullNameExists) {
                    continue;
                }
                // Solo agregar si active es true o null
                if (dto.getActive() != null && !dto.getActive()) {
                    continue;
                }
                nullNameAdded = true;
                Category existingNull = existingCategories.stream().filter(c -> c.getName() == null).findFirst().orElse(null);
                if (existingNull != null) {
                    resultCategories.add(new CategoryDTO(Long.valueOf(existingNull.getId()), null, existingNull.isActive()));
                } else {
                    Category c = new Category();
                    c.setName(null);
                    c.setActive(dto.getActive() != null ? dto.getActive() : true);
                    Category saved = categoryService.saveCategory(c);
                    if (saved != null) {
                        resultCategories.add(new CategoryDTO(Long.valueOf(saved.getId()), saved.getName(), saved.isActive()));
                        existingCategories.add(saved);
                    }
                }
                continue;
            }
            String nameLower = dto.getName().toLowerCase();
            if (processedNames.contains(nameLower)) {
                continue;
            }
            processedNames.add(nameLower);
            Category existing = existingCategories.stream()
                    .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(dto.getName()))
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                resultCategories.add(new CategoryDTO(Long.valueOf(existing.getId()), existing.getName(), existing.isActive()));
            } else {
                Category c = new Category();
                c.setName(dto.getName());
                c.setActive(dto.getActive() != null ? dto.getActive() : true);
                Category saved = categoryService.saveCategory(c);
                if (saved != null) {
                    resultCategories.add(new CategoryDTO(Long.valueOf(saved.getId()), saved.getName(), saved.isActive()));
                    existingCategories.add(saved);
                }
            }
        }
        return resultCategories;
    }

    @PatchMapping("/{id}/activate")
    public CategoryDTO activateCategory(@PathVariable Integer id) {
        Category category = categoryService.getAllCategories().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        category.setActive(true);
        Category updated = categoryService.saveCategory(category);
        return new CategoryDTO(Long.valueOf(updated.getId()), updated.getName(), updated.isActive());
    }
}
