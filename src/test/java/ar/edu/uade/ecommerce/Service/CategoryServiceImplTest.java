package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void testSaveAllCategories_updateCreateDelete() {
        Category c1 = new Category(); c1.setId(1); c1.setName("Electrónica"); c1.setActive(true);
        Category c2 = new Category(); c2.setId(2); c2.setName("Ropa"); c2.setActive(true);
        Category c3 = new Category(); c3.setId(3); c3.setName("Hogar"); c3.setActive(true);
        List<Category> existing = Arrays.asList(c1, c2, c3);
        Category incoming1 = new Category(); incoming1.setName("Electrónica");
        Category incoming2 = new Category(); incoming2.setName("Deportes");
        List<Category> incoming = Arrays.asList(incoming1, incoming2);
        when(categoryRepository.findAll()).thenReturn(existing).thenReturn(Arrays.asList(c1, incoming2));
        when(categoryRepository.findByName("Electrónica")).thenReturn(Optional.of(c1));
        when(categoryRepository.findByName("Deportes")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        List<Category> result = categoryService.saveAllCategories(incoming);
        assertNotNull(result);
        verify(categoryRepository).delete(c2);
        verify(categoryRepository).delete(c3);
        verify(categoryRepository).save(c1);
        verify(categoryRepository).save(incoming2);
    }

    @Test
    void testSaveAllCategories_noChanges() {
        Category c1 = new Category(); c1.setId(1); c1.setName("Electrónica"); c1.setActive(true);
        List<Category> existing = Collections.singletonList(c1);
        List<Category> incoming = Collections.singletonList(c1);
        when(categoryRepository.findAll()).thenReturn(existing).thenReturn(existing);
        when(categoryRepository.findByName("Electrónica")).thenReturn(Optional.of(c1));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        List<Category> result = categoryService.saveAllCategories(incoming);
        assertEquals(1, result.size());
        verify(categoryRepository, never()).delete(any());
        verify(categoryRepository).save(c1);
    }

    @Test
    void testSaveCategory_nullCategory() {
        Category result = categoryService.saveCategory(null);
        assertNull(result);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void testSaveCategory_nullName() {
        Category category = new Category();
        category.setName(null);
        Category result = categoryService.saveCategory(category);
        assertNull(result);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void testSaveCategory_activeFalseWithId() {
        Category category = new Category();
        category.setId(1);
        category.setName("Electrónica");
        category.setActive(false);
        when(categoryRepository.save(category)).thenReturn(category);
        Category result = categoryService.saveCategory(category);
        assertFalse(result.isActive());
        verify(categoryRepository).save(category);
    }

    @Test
    void testSaveCategory_activeTrueOrNull() {
        Category category = new Category();
        category.setName("Electrónica");
        category.setActive(true);
        when(categoryRepository.save(category)).thenReturn(category);
        Category result = categoryService.saveCategory(category);
        assertTrue(result.isActive());
        verify(categoryRepository).save(category);
    }

    @Test
    void testSaveCategory_activeFalseWithNullId() {
        Category category = new Category();
        category.setId(null);
        category.setName("Electrónica");
        category.setActive(false);
        when(categoryRepository.save(category)).thenReturn(category);
        Category result = categoryService.saveCategory(category);
        assertTrue(result.isActive());
        verify(categoryRepository).save(category);
    }

    @Test
    void testGetAllCategories() {
        Category c1 = new Category(); c1.setName("Electrónica");
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(c1));
        List<Category> result = categoryService.getAllCategories();
        assertEquals(1, result.size());
        assertEquals("Electrónica", result.get(0).getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void testDeleteAllCategories() {
        categoryService.deleteAllCategories();
        verify(categoryRepository).deleteAll();
    }
}
