//package ar.edu.uade.ecommerce.Controllers;
//
//import ar.edu.uade.ecommerce.Entity.Category;
//import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
//import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
//import ar.edu.uade.ecommerce.Service.CategoryService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import java.util.List;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CategoryControllerTest {
//    @Mock
//    private CategoryService categoryService;
//    @Mock
//    private KafkaMockService kafkaMockService;
//    @InjectMocks
//    private CategoryController categoryController;
//
//    // Métodos válidos según el CategoryController actual:
//    @Test
//    void testGetAllCategories() {
//        Category category1 = new Category();
//        category1.setId(1);
//        category1.setName("Accesorios");
//        category1.setActive(true);
//        Category category2 = new Category();
//        category2.setId(2);
//        category2.setName("Celulares");
//        category2.setActive(true);
//        List<Category> categories = List.of(category1, category2);
//        when(categoryService.getAllCategories()).thenReturn(categories);
//        List<CategoryDTO> result = categoryController.getAllCategories();
//        assertEquals(2, result.size());
//        assertEquals("Accesorios", result.get(0).getName());
//    }
//
//    @Test
//    void testSyncCategoriesFromMock_EmptyMock() {
//        KafkaMockService.CategorySyncMessage emptyMessage = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of()), "2025-09-02T08:29:02.072020100");
//        when(kafkaMockService.getCategoriesMock()).thenReturn(emptyMessage);
//        when(categoryService.getAllCategories()).thenReturn(List.of());
//        List<CategoryDTO> result = categoryController.syncCategoriesFromMock();
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void testSyncCategoriesFromMock_AllExist() {
//        CategoryDTO mockCategory1 = new CategoryDTO(1L, "Celulares", true);
//        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(mockCategory1)), "2025-09-02T08:29:02.072020100");
//        Category existingCategory = new Category();
//        existingCategory.setId(1);
//        existingCategory.setName("Celulares");
//        existingCategory.setActive(true);
//        when(categoryService.getAllCategories()).thenReturn(List.of(existingCategory));
//        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
//        List<CategoryDTO> result = categoryController.syncCategoriesFromMock();
//        assertEquals(1, result.size());
//        assertEquals("Celulares", result.get(0).getName());
//    }
//
//    @Test
//    void testAddCategoryFromMock() {
//        CategoryDTO mockCategory = new CategoryDTO(2L, "Tablets", true);
//        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(mockCategory)), "2025-09-02T08:29:02.072020100");
//        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
//        Category category = new Category();
//        category.setId(2);
//        category.setName("Tablets");
//        category.setActive(true);
//        when(categoryService.getAllCategories()).thenReturn(List.of());
//        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
//        CategoryDTO result = categoryController.addCategoryFromMock();
//        assertEquals("Tablets", result.getName());
//        assertTrue(result.getActive());
//    }
//
//    @Test
//    void testActivateCategoryFromMock() {
//        CategoryDTO mockCategory = new CategoryDTO(3L, "Monitores", false);
//        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(mockCategory)), "2025-09-02T08:29:02.072020100");
//        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
//        Category category = new Category();
//        category.setId(3);
//        category.setName("Monitores");
//        category.setActive(false);
//        when(categoryService.getAllCategories()).thenReturn(List.of(category));
//        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
//        CategoryDTO result = categoryController.activateCategoryFromMock();
//        assertEquals("Monitores", result.getName());
//        assertTrue(result.getActive());
//    }
//
//    @Test
//    void testDeactivateCategoryFromMock() {
//        CategoryDTO mockCategory = new CategoryDTO(4L, "Notebooks", true);
//        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(mockCategory)), "2025-09-02T08:29:02.072020100");
//        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
//        Category category = new Category();
//        category.setId(4);
//        category.setName("Notebooks");
//        category.setActive(true);
//        when(categoryService.getAllCategories()).thenReturn(List.of(category));
//        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
//        CategoryDTO result = categoryController.deactivateCategoryFromMock();
//        assertEquals("Notebooks", result.getName());
//        assertFalse(result.getActive());
//    }
//
//    @Test
//    void testUpdateCategoryFromMock() {
//        CategoryDTO mockCategory = new CategoryDTO(5L, "Smartwatches", null);
//        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(mockCategory)), "2025-09-02T08:29:02.072020100");
//        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
//        Category category = new Category();
//        category.setId(1);
//        category.setName("Relojes");
//        category.setActive(false);
//        when(categoryService.getAllCategories()).thenReturn(List.of(category));
//        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
//        CategoryDTO result = categoryController.updateCategoryFromMock();
//        assertEquals("Smartwatches", result.getName());
//        assertFalse(result.getActive()); // El mock no cambia el active si es null
//    }
//}
