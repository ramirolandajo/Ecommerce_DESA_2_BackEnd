package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplNewTests {
    @Mock CategoryRepository categoryRepository;
    @InjectMocks CategoryServiceImpl service;

    @Test
    void saveCategory_nullOrNameNull_returnsNull() {
        assertNull(service.saveCategory(null));
        Category c = new Category();
        assertNull(service.saveCategory(c));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void saveCategory_setsActiveTrueForNew() {
        Category c = new Category(); c.setName("C");
        when(categoryRepository.save(c)).thenReturn(c);
        Category out = service.saveCategory(c);
        assertTrue(out.isActive());
        verify(categoryRepository).save(c);
    }

    @Test
    void saveAllCategories_whenIncomingContainsExisting_updatesAndCreates_withoutDelete() {
        Category existing = new Category(); existing.setId(1); existing.setName("OLD"); existing.setActive(true);
        Category incoming1 = new Category(); incoming1.setName("NEW1");
        Category incoming2 = new Category(); incoming2.setName("OLD");

        when(categoryRepository.findAll()).thenReturn(List.of(existing), List.of(incoming1, existing));
        when(categoryRepository.findByName("NEW1")).thenReturn(Optional.empty());
        when(categoryRepository.findByName("OLD")).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Category> result = service.saveAllCategories(List.of(incoming1, incoming2));
        assertEquals(2, result.size());
        verify(categoryRepository, never()).delete(any());
        verify(categoryRepository, atLeastOnce()).save(any(Category.class));
    }

    @Test
    void saveAllCategories_whenIncomingDoesNotContainExisting_deletesMissing() {
        Category existing = new Category(); existing.setId(1); existing.setName("OLD"); existing.setActive(true);
        Category incoming1 = new Category(); incoming1.setName("NEW1");

        when(categoryRepository.findAll()).thenReturn(List.of(existing), List.of(incoming1));
        when(categoryRepository.findByName("NEW1")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Category> result = service.saveAllCategories(List.of(incoming1));
        assertEquals(1, result.size());
        verify(categoryRepository).delete(existing);
        verify(categoryRepository, atLeastOnce()).save(any(Category.class));
    }

    @Test
    void getAllActiveCategories_delegates() {
        when(categoryRepository.findByActiveTrue()).thenReturn(List.of(new Category()));
        assertEquals(1, service.getAllActiveCategories().size());
    }
}
