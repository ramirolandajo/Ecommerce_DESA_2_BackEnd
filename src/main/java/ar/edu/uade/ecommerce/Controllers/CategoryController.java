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
        List<CategoryDTO> mockCategories = kafkaMockService.getCategoriesMock();
        List<Category> existingCategories = categoryService.getAllCategories();
        for (CategoryDTO dto : mockCategories) {
            Category existing = existingCategories.stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(dto.getName()))
                .findFirst()
                .orElse(null);
            if (existing == null) {
                Category c = new Category();
                c.setName(dto.getName());
                c.setActive(true);
                categoryService.saveCategory(c);
            }
        }
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
        category.setName(categoryDTO.getName());
        if (categoryDTO.getActive() == null) {
            category.setActive(category.isActive());
        } else {
            category.setActive(categoryDTO.getActive());
        }
        Category updated = categoryService.saveCategory(category);
        return new CategoryDTO(Long.valueOf(updated.getId()), updated.getName(), updated.isActive());
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
        for (CategoryDTO dto : categoryDTOs) {
            boolean exists = categoryService.getAllCategories().stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(dto.getName()));
            if (!exists) {
                Category c = new Category();
                c.setName(dto.getName());
                c.setActive(dto.getActive() != null ? dto.getActive() : true);
                categoryService.saveCategory(c);
            }
        }
        return categoryService.getAllCategories().stream()
                .map(c -> new CategoryDTO(Long.valueOf(c.getId()), c.getName(), c.isActive()))
                .collect(Collectors.toList());
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
