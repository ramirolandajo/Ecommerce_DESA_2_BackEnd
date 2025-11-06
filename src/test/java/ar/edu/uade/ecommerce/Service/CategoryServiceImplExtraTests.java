package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplExtraTests {
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void testSaveCategory_whenRepositoryThrowsException() {
        Category category = new Category();
        category.setName("Electrónica");
        when(categoryRepository.save(category)).thenThrow(new RuntimeException("Save error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.saveCategory(category));
        assertEquals("Save error", ex.getMessage());
    }

    @Test
    void testGetAllCategories_whenRepositoryThrowsException() {
        when(categoryRepository.findAll()).thenThrow(new RuntimeException("Find all error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.getAllCategories());
        assertEquals("Find all error", ex.getMessage());
    }

    @Test
    void testSaveAllCategories_whenFindAllThrowsException() {
        when(categoryRepository.findAll()).thenThrow(new RuntimeException("Find all error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.saveAllCategories(Collections.emptyList()));
        assertEquals("Find all error", ex.getMessage());
    }

    @Test
    void testSaveAllCategories_whenFindByNameThrowsException() {
        Category incoming = new Category();
        incoming.setName("Electrónica");
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(categoryRepository.findByName("Electrónica")).thenThrow(new RuntimeException("Find by name error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.saveAllCategories(List.of(incoming)));
        assertEquals("Find by name error", ex.getMessage());
    }

    @Test
    void testDeleteAllCategories_whenRepositoryThrowsException() {
        doThrow(new RuntimeException("Delete all error")).when(categoryRepository).deleteAll();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.deleteAllCategories());
        assertEquals("Delete all error", ex.getMessage());
    }
}
