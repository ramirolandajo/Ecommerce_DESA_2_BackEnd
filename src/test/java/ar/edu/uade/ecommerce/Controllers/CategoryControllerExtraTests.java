package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerExtraTests {
    @Mock
    private CategoryService categoryService;
    @Mock
    private KafkaMockService kafkaMockService;
    @InjectMocks
    private CategoryController categoryController;

    @Test
    void testAddBulkCategories_nullOrEmptyInput() {
        assertTrue(categoryController.addBulkCategories(null).isEmpty());
        assertTrue(categoryController.addBulkCategories(List.of()).isEmpty());
    }

    @Test
    void testAddBulkCategories_singleNullName_addedWhenNotExistsAndActiveNull() {
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved = new Category(); saved.setId(11); saved.setName(null); saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);

        CategoryDTO dto = new CategoryDTO(null, null, null);
        List<CategoryDTO> res = categoryController.addBulkCategories(List.of(dto));
        assertEquals(1, res.size());
        assertNull(res.get(0).getName());
        assertTrue(res.get(0).getActive());
        verify(categoryService).saveCategory(any());
    }

    @Test
    void testAddBulkCategories_nullName_skipIfActiveFalse() {
        when(categoryService.getAllCategories()).thenReturn(List.of());
        CategoryDTO dto = new CategoryDTO(null, null, false);
        List<CategoryDTO> res = categoryController.addBulkCategories(List.of(dto));
        assertTrue(res.isEmpty());
        verify(categoryService, never()).saveCategory(any());
    }

    @Test
    void testAddBulkCategories_duplicateNames_caseInsensitive_onlyOneSaved() {
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved = new Category(); saved.setId(21); saved.setName("Accesorios"); saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);

        CategoryDTO d1 = new CategoryDTO(null, "Accesorios", null);
        CategoryDTO d2 = new CategoryDTO(null, "acCESorios", null);
        List<CategoryDTO> res = categoryController.addBulkCategories(List.of(d1, d2));
        assertEquals(1, res.size());
        assertEquals("Accesorios", res.get(0).getName());
        verify(categoryService, times(1)).saveCategory(any());
    }

    @Test
    void testAddBulkCategories_existingCategory_returnExistingAndDontSave() {
        Category existing = new Category(); existing.setId(30); existing.setName("Celulares"); existing.setActive(false);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing));
        CategoryDTO dto = new CategoryDTO(null, "Celulares", null);
        List<CategoryDTO> res = categoryController.addBulkCategories(List.of(dto));
        assertEquals(1, res.size());
        assertEquals("Celulares", res.get(0).getName());
        assertFalse(res.get(0).getActive());
        verify(categoryService, never()).saveCategory(any());
    }

    @Test
    void testAddBulkCategories_mixedExistingAndNew_andDuplicates() {
        Category existing = new Category(); existing.setId(40); existing.setName("Ropa"); existing.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(new java.util.ArrayList<>(List.of(existing)));
        Category savedNew = new Category(); savedNew.setId(41); savedNew.setName("Zapatos"); savedNew.setActive(true);
        Category savedNull = new Category(); savedNull.setId(42); savedNull.setName(null); savedNull.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            if (arg.getName() == null) return savedNull;
            return savedNew;
        });

        CategoryDTO a = new CategoryDTO(null, "Ropa", null); // existing
        CategoryDTO b = new CategoryDTO(null, "Zapatos", null); // new
        CategoryDTO c = new CategoryDTO(null, "zapatos", null); // duplicate in same request
        CategoryDTO d = new CategoryDTO(null, null, null); // null name -> allowed once
        List<CategoryDTO> res = categoryController.addBulkCategories(List.of(a, b, c, d));
        assertEquals(3, res.size());
        // contains Ropa, Zapatos, and null-name
        assertTrue(res.stream().anyMatch(x -> "Ropa".equals(x.getName())));
        assertTrue(res.stream().anyMatch(x -> "Zapatos".equals(x.getName())));
        assertTrue(res.stream().anyMatch(x -> x.getName() == null));
        verify(categoryService, times(1)).saveCategory(argThat(cat -> "Zapatos".equals(cat.getName())));
    }

    @Test
    void testAddCategoryFromMock_newSaved() {
        CategoryDTO mockCategory = new CategoryDTO(6L, "Nuevacat", true);
        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(mockCategory)), "ts");
        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved = new Category(); saved.setId(6); saved.setName("Nuevacat"); saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);

        CategoryDTO res = categoryController.addCategoryFromMock();
        assertNotNull(res);
        assertEquals("Nuevacat", res.getName());
        assertTrue(res.getActive());
        verify(categoryService).saveCategory(any());
    }

    @Test
    void testUpdateCategoryFromMock_nameMatched() {
        CategoryDTO mockCategory = new CategoryDTO(7L, "Matched", false);
        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(mockCategory)), "ts");
        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
        Category existing1 = new Category(); existing1.setId(70); existing1.setName("Matched"); existing1.setActive(true);
        Category other = new Category(); other.setId(71); other.setName("Other"); other.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing1, other));
        Category updated = new Category(); updated.setId(70); updated.setName("Matched"); updated.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(updated);

        CategoryDTO res = categoryController.updateCategoryFromMock();
        assertNotNull(res);
        assertEquals("Matched", res.getName());
        assertFalse(res.getActive());
        verify(categoryService).saveCategory(argThat(c -> c.getName().equals("Matched")));
    }

    @Test
    void testSyncCategoriesFromMock_emptyPayload_noChange() {
        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of()), "ts");
        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        List<CategoryDTO> out = categoryController.syncCategoriesFromMock();
        assertNotNull(out);
        assertTrue(out.isEmpty());
        verify(categoryService, never()).saveCategory(any());
    }

    @Test
    void testAddBulkCategories_multipleNullNames_onlyOneAdded() {
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category savedNull = new Category(); savedNull.setId(60); savedNull.setName(null); savedNull.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(savedNull);

        CategoryDTO d1 = new CategoryDTO(null, null, null);
        CategoryDTO d2 = new CategoryDTO(null, null, null);
        List<CategoryDTO> res = categoryController.addBulkCategories(List.of(d1, d2));
        assertEquals(1, res.size());
        assertNull(res.get(0).getName());
        verify(categoryService, times(1)).saveCategory(argThat(c -> c.getName() == null));
    }

    @Test
    void testAddBulkCategories_processedNames_deduplicatesAcrossOrder() {
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category savedA = new Category(); savedA.setId(61); savedA.setName("A"); savedA.setActive(true);
        Category savedB = new Category(); savedB.setId(62); savedB.setName("B"); savedB.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            if ("A".equalsIgnoreCase(arg.getName())) return savedA;
            return savedB;
        });

        CategoryDTO x1 = new CategoryDTO(null, "A", null);
        CategoryDTO x2 = new CategoryDTO(null, "B", null);
        CategoryDTO x3 = new CategoryDTO(null, "a", null);
        CategoryDTO x4 = new CategoryDTO(null, "A", null);
        List<CategoryDTO> res = categoryController.addBulkCategories(List.of(x1, x2, x3, x4));
        assertEquals(2, res.size());
        assertTrue(res.stream().anyMatch(c -> "A".equals(c.getName())));
        assertTrue(res.stream().anyMatch(c -> "B".equals(c.getName())));
        verify(categoryService, times(2)).saveCategory(any());
    }

    @Test
    void testSyncCategoriesFromMock_nullNameAdded() {
        CategoryDTO incoming = new CategoryDTO(9L, null, true);
        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(incoming)), "ts");
        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
        // avoid unchecked generics varargs warning by creating local typed lists
        List<Category> firstCall = List.of();
        List<Category> secondCall = List.of(new Category());
        when(categoryService.getAllCategories()).thenReturn(firstCall, secondCall);
        Category saved = new Category(); saved.setId(63); saved.setName(null); saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);

        List<CategoryDTO> out = categoryController.syncCategoriesFromMock();
        assertNotNull(out);
        // final list mocked as return of getAllCategories second call; at least ensure saveCategory called
        verify(categoryService, times(1)).saveCategory(argThat(c -> c.getName() == null));
    }

    @Test
    void testUpdateCategoryFromMock_activeNull_keepsExistingActive() {
        CategoryDTO mockCategory = new CategoryDTO(10L, "KeepActive", null);
        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(mockCategory)), "ts");
        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
        Category existing = new Category(); existing.setId(70); existing.setName("KeepActive"); existing.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing));
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryDTO res = categoryController.updateCategoryFromMock();
        assertNotNull(res);
        // since incoming active is null, controller should not change active status (remains true)
        assertTrue(res.getActive());
        verify(categoryService).saveCategory(argThat(c -> c.isActive()));
    }

    @Test
    void testAddCategoryFromMock_existingNoSave() {
        CategoryDTO mockCategory = new CategoryDTO(2L, "Tablets", true);
        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(mockCategory)), "2025-09-02T08:29:02.072020100");
        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
        Category existing = new Category(); existing.setId(2); existing.setName("Tablets"); existing.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing));

        CategoryDTO result = categoryController.addCategoryFromMock();
        assertNotNull(result);
        assertEquals("Tablets", result.getName());
        verify(categoryService, never()).saveCategory(any());
    }
}
