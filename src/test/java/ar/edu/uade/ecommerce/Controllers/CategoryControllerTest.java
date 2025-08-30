package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.Service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryControllerTest {
    @Mock
    private CategoryService categoryService;
    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllCategories() {
        Category category1 = new Category();
        category1.setId(1);
        category1.setName("Accesorios");
        category1.setActive(true);
        Category category2 = new Category();
        category2.setId(2);
        category2.setName("Celulares");
        category2.setActive(true);
        List<Category> categories = List.of(category1, category2);
        when(categoryService.getAllCategories()).thenReturn(categories);
        List<CategoryDTO> result = categoryController.getAllCategories();
        assertEquals(2, result.size());
        assertEquals("Accesorios", result.get(0).getName());
    }

    @Test
    void testAddCategory() {
        CategoryDTO dto = new CategoryDTO(1L, "Tablets", true);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.addCategory(dto);
        assertEquals("Tablets", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testUpdateCategory() {
        CategoryDTO dto = new CategoryDTO(1L, "Smartphones", false);
        Category category = new Category();
        category.setId(1);
        category.setName("Smartphones");
        category.setActive(false);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.updateCategory(1, dto);
        assertEquals("Smartphones", result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testDeleteCategory() {
        Category category = new Category();
        category.setId(1);
        category.setName("Notebooks");
        category.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        List<CategoryDTO> result = categoryController.deleteCategory(1);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getActive());
    }

    @Test
    void testActivateCategory() {
        Category category = new Category();
        category.setId(1);
        category.setName("Monitores");
        category.setActive(false);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.activateCategory(1);
        assertEquals("Monitores", result.getName());
        assertTrue(result.getActive());
    }
}

