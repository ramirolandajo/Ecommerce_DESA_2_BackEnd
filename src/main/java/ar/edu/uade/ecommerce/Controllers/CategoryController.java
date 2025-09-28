package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // helper para convertir Integer id a Long manejando nulls
    private Long toLongId(Integer id) {
        return id == null ? null : Long.valueOf(id.longValue());
    }

    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategories().stream()
                .map(c -> new CategoryDTO(toLongId(c.getId()), c.getName(), c.isActive()))
                .collect(Collectors.toList());
    }

    // Endpoints mock deshabilitados: la sincronización de categorías debe venir desde la API de Comunicación
    @GetMapping("/sync")
    public List<CategoryDTO> syncCategoriesFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint de sincronización mock deshabilitado. La sincronización debe venir desde la API de Comunicación (Core).");
    }

    @PostMapping("/mock/add")
    public CategoryDTO addCategoryFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use la API de Comunicación para sincronizar categorías.");
    }

    @PatchMapping("/mock/activate")
    public CategoryDTO activateCategoryFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use la API de Comunicación para sincronizar categorías.");
    }

    @PatchMapping("/mock/deactivate")
    public CategoryDTO deactivateCategoryFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use la API de Comunicación para sincronizar categorías.");
    }

    @PatchMapping("/mock/update")
    public CategoryDTO updateCategoryFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use la API de Comunicación para sincronizar categorías.");
    }

    // Endpoints reales
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
                    resultCategories.add(new CategoryDTO(toLongId(existingNull.getId()), null, existingNull.isActive()));
                } else {
                    Category c = new Category();
                    c.setName(null);
                    c.setActive(dto.getActive() != null ? dto.getActive() : true);
                    Category saved = categoryService.saveCategory(c);
                    if (saved != null) {
                        resultCategories.add(new CategoryDTO(toLongId(saved.getId()), saved.getName(), saved.isActive()));
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
                resultCategories.add(new CategoryDTO(toLongId(existing.getId()), existing.getName(), existing.isActive()));
            } else {
                Category c = new Category();
                c.setName(dto.getName());
                c.setActive(dto.getActive() != null ? dto.getActive() : true);
                Category saved = categoryService.saveCategory(c);
                if (saved != null) {
                    resultCategories.add(new CategoryDTO(toLongId(saved.getId()), saved.getName(), saved.isActive()));
                    existingCategories.add(saved);
                }
            }
        }
        return resultCategories;
    }
}
