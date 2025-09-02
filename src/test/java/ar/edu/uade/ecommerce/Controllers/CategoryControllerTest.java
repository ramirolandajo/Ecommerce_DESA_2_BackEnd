package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
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
    @Mock
    private KafkaMockService kafkaMockService;
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

    @Test
    void testSyncCategoriesFromMock() {
        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", true);
        CategoryDTO dto2 = new CategoryDTO(2L, "Celulares", true);
        List<CategoryDTO> mockDtos = List.of(dto1, dto2);
        KafkaMockService.CategorySyncPayload payload = new KafkaMockService.CategorySyncPayload(mockDtos);
        KafkaMockService.CategorySyncMessage mockMessage = new KafkaMockService.CategorySyncMessage("CategorySync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getCategoriesMock()).thenReturn(mockMessage);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved1 = new Category();
        saved1.setId(1);
        saved1.setName("Accesorios");
        saved1.setActive(true);
        Category saved2 = new Category();
        saved2.setId(2);
        saved2.setName("Celulares");
        saved2.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved1, saved2);
        when(categoryService.getAllCategories()).thenReturn(List.of(saved1, saved2));
        List<CategoryDTO> result = categoryController.syncCategoriesFromMock();
        assertEquals(2, result.size());
        assertEquals("Accesorios", result.get(0).getName());
        assertEquals("Celulares", result.get(1).getName());
    }

    @Test
    void testAddBulkCategories() {
        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", true);
        CategoryDTO dto2 = new CategoryDTO(2L, "Celulares", true);
        List<CategoryDTO> dtos = List.of(dto1, dto2);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved1 = new Category();
        saved1.setId(1);
        saved1.setName("Accesorios");
        saved1.setActive(true);
        Category saved2 = new Category();
        saved2.setId(2);
        saved2.setName("Celulares");
        saved2.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved1, saved2);
        when(categoryService.getAllCategories()).thenReturn(List.of(saved1, saved2));
        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
        assertEquals(2, result.size());
        assertEquals("Accesorios", result.get(0).getName());
        assertEquals("Celulares", result.get(1).getName());
    }

    @Test
    void testAddBulkCategories_existingCategory() {
        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", true);
        CategoryDTO dto2 = new CategoryDTO(2L, "Celulares", true);
        List<CategoryDTO> dtos = List.of(dto1, dto2);
        Category existing = new Category();
        existing.setId(1);
        existing.setName("Accesorios");
        existing.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing));
        // No se debe guardar la categoría existente
        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
        assertEquals(1, result.size());
        assertEquals("Accesorios", result.get(0).getName());
    }

    @Test
    void testAddBulkCategories_emptyList() {
        List<CategoryDTO> dtos = List.of();
        when(categoryService.getAllCategories()).thenReturn(List.of());
        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddBulkCategories_nullActive() {
        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", null);
        List<CategoryDTO> dtos = List.of(dto1);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved = new Category();
        saved.setId(1);
        saved.setName("Accesorios");
        saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);
        when(categoryService.getAllCategories()).thenReturn(List.of(saved));
        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
    }

//    @Test
//    void testAddBulkCategories_nullName() {
//        CategoryDTO dto1 = new CategoryDTO(1L, null, true);
//        List<CategoryDTO> dtos = List.of(dto1);
//        when(categoryService.getAllCategories()).thenReturn(List.of());
//        Category saved = new Category();
//        saved.setId(1);
//        saved.setName(null);
//        saved.setActive(true);
//        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);
//        when(categoryService.getAllCategories()).thenReturn(List.of(saved));
//        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
//        assertEquals(1, result.size());
//        assertNull(result.get(0).getName());
//    }

    @Test
    void testSyncCategoriesFromMock_nullName() {
        CategoryDTO dto1 = new CategoryDTO(1L, null, true);
        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(dto1)), "2025-09-02T08:29:02.072020100");
        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved = new Category();
        saved.setId(1);
        saved.setName(null);
        saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);
        when(categoryService.getAllCategories()).thenReturn(List.of(saved));
        List<CategoryDTO> result = categoryController.syncCategoriesFromMock();
        assertEquals(1, result.size());
        assertNull(result.get(0).getName());
    }

    @Test
    void testActivateCategory_notFound() {
        when(categoryService.getAllCategories()).thenReturn(List.of());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryController.activateCategory(99));
        assertEquals("Categoría no encontrada", ex.getMessage());
    }

    @Test
    void testDeleteCategory_notFound() {
        when(categoryService.getAllCategories()).thenReturn(List.of());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryController.deleteCategory(99));
        assertEquals("Categoría no encontrada", ex.getMessage());
    }

    @Test
    void testUpdateCategory_notFound() {
        when(categoryService.getAllCategories()).thenReturn(List.of());
        CategoryDTO dto = new CategoryDTO(99L, "No existe", true);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryController.updateCategory(99, dto));
        assertEquals("Categoría no encontrada", ex.getMessage());
    }

    @Test
    void testAddBulkCategories_duplicateNamesInList() {
        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", true);
        CategoryDTO dto2 = new CategoryDTO(2L, "Accesorios", true);
        List<CategoryDTO> dtos = List.of(dto1, dto2);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved = new Category();
        saved.setId(1);
        saved.setName("Accesorios");
        saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);
        when(categoryService.getAllCategories()).thenReturn(List.of(saved));
        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
        assertEquals(1, result.size());
        assertEquals("Accesorios", result.get(0).getName());
    }

    @Test
    void testAddBulkCategories_caseInsensitive() {
        CategoryDTO dto1 = new CategoryDTO(1L, "accesorios", true);
        Category existing = new Category();
        existing.setId(2);
        existing.setName("Accesorios");
        existing.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing));
        List<CategoryDTO> result = categoryController.addBulkCategories(List.of(dto1));
        assertEquals(1, result.size());
        assertEquals("Accesorios", result.get(0).getName());
    }

    @Test
    void testAddBulkCategories_multipleDuplicatesAndCaseInsensitive() {
        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", true);
        CategoryDTO dto2 = new CategoryDTO(2L, "accesorios", true);
        CategoryDTO dto3 = new CategoryDTO(3L, "Celulares", true);
        CategoryDTO dto4 = new CategoryDTO(4L, "CELULARES", true);
        List<CategoryDTO> dtos = List.of(dto1, dto2, dto3, dto4);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved1 = new Category();
        saved1.setId(1);
        saved1.setName("Accesorios");
        saved1.setActive(true);
        Category saved2 = new Category();
        saved2.setId(2);
        saved2.setName("Celulares");
        saved2.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved1, saved2);
        when(categoryService.getAllCategories()).thenReturn(List.of(saved1, saved2));
        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Accesorios")));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Celulares")));
    }

    @Test
    void testAddBulkCategories_existingAndNewCategory() {
        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", true);
        CategoryDTO dto2 = new CategoryDTO(2L, "Nuevo", true);
        Category existing = new Category();
        existing.setId(1);
        existing.setName("Accesorios");
        existing.setActive(true);
        Category saved = new Category();
        saved.setId(2);
        saved.setName("Nuevo");
        saved.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing, saved));
        List<CategoryDTO> result = categoryController.addBulkCategories(List.of(dto1, dto2));
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Accesorios")));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Nuevo")));
    }

    @Test
    void testAddCategory_activeFalse() {
        CategoryDTO dto = new CategoryDTO(1L, "Tablets", false);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.addCategory(dto);
        assertEquals("Tablets", result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testUpdateCategory_activeFalse() {
        CategoryDTO dto = new CategoryDTO(1L, "Tablets", false);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(false);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.updateCategory(1, dto);
        assertEquals("Tablets", result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testAddCategory_nullActive() {
        CategoryDTO dto = new CategoryDTO(1L, "Tablets", null);
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
    void testAddCategory_falseActive() {
        CategoryDTO dto = new CategoryDTO(1L, "Tablets", false);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.addCategory(dto);
        assertEquals("Tablets", result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testUpdateCategory_nullActive() {
        CategoryDTO dto = new CategoryDTO(1L, "Tablets", null);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.updateCategory(1, dto);
        assertEquals("Tablets", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testUpdateCategory_falseActive() {
        CategoryDTO dto = new CategoryDTO(1L, "Tablets", false);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(false);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.updateCategory(1, dto);
        assertEquals("Tablets", result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testAddCategory_nullName() {
        CategoryDTO dto = new CategoryDTO(1L, null, true);
        Category category = new Category();
        category.setId(1);
        category.setName(null);
        category.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.addCategory(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testUpdateCategory_nullName() {
        CategoryDTO dto = new CategoryDTO(1L, null, true);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        category.setName(null); // Simula el cambio de nombre a null
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.updateCategory(1, dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testActivateCategory_alreadyActive() {
        Category category = new Category();
        category.setId(1);
        category.setName("Monitores");
        category.setActive(true); // Ya está activa
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.activateCategory(1);
        assertEquals("Monitores", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testAddBulkCategories_lambdaCoverage() {
        // Este test fuerza la ejecución de la lambda interna de addBulkCategories
        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", true);
        CategoryDTO dto2 = new CategoryDTO(2L, "Celulares", true);
        Category existing = new Category();
        existing.setId(1);
        existing.setName("Accesorios");
        existing.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing));
        Category saved = new Category();
        saved.setId(2);
        saved.setName("Celulares");
        saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing, saved));
        List<CategoryDTO> result = categoryController.addBulkCategories(List.of(dto1, dto2));
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Accesorios")));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Celulares")));
    }

    @Test
    void testUpdateCategory_lambdaCoverage() {
        // Este test fuerza la ejecución de la lambda interna de updateCategory
        CategoryDTO dto = new CategoryDTO(1L, "Smartphones", true);
        Category category = new Category();
        category.setId(1);
        category.setName("Smartphones");
        category.setActive(false);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        category.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.updateCategory(1, dto);
        assertEquals("Smartphones", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testAddCategory_lambdaCoverage() {
        // Este test fuerza la ejecución de la lambda interna de addCategory
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
    void testAddBulkCategories_branchCoverage() {
        // Prueba con una lista que tiene duplicados y case-insensitive, y una categoría existente en la base
        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", true);
        CategoryDTO dto2 = new CategoryDTO(2L, "accesorios", true);
        CategoryDTO dto3 = new CategoryDTO(3L, "Celulares", true);
        CategoryDTO dto4 = new CategoryDTO(4L, "CELULARES", true);
        CategoryDTO dto5 = new CategoryDTO(5L, "Nuevo", true);
        Category existing = new Category();
        existing.setId(1);
        existing.setName("Accesorios");
        existing.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing));
        Category savedCelulares = new Category();
        savedCelulares.setId(2);
        savedCelulares.setName("Celulares");
        savedCelulares.setActive(true);
        Category savedNuevo = new Category();
        savedNuevo.setId(3);
        savedNuevo.setName("Nuevo");
        savedNuevo.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(savedCelulares, savedNuevo);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing, savedCelulares, savedNuevo));
        List<CategoryDTO> result = categoryController.addBulkCategories(List.of(dto1, dto2, dto3, dto4, dto5));
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Accesorios")));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Celulares")));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Nuevo")));
    }

    @Test
    void testUpdateCategory_branchCoverage() {
        // Prueba con active en null y en false, y name en null
        CategoryDTO dtoNullActive = new CategoryDTO(1L, "Tablets", null);
        CategoryDTO dtoFalseActive = new CategoryDTO(1L, "Tablets", false);
        CategoryDTO dtoNullName = new CategoryDTO(1L, null, true);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        // null active
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO resultNullActive = categoryController.updateCategory(1, dtoNullActive);
        assertEquals("Tablets", resultNullActive.getName());
        assertTrue(resultNullActive.getActive());
        // false active
        category.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO resultFalseActive = categoryController.updateCategory(1, dtoFalseActive);
        assertEquals("Tablets", resultFalseActive.getName());
        assertFalse(resultFalseActive.getActive());
        // null name
        category.setName(null);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO resultNullName = categoryController.updateCategory(1, dtoNullName);
        assertNull(resultNullName.getName());
    }

    @Test
    void testAddCategory_branchCoverage() {
        // Prueba con active en null, en false y name en null
        CategoryDTO dtoNullActive = new CategoryDTO(1L, "Tablets", null);
        CategoryDTO dtoFalseActive = new CategoryDTO(1L, "Tablets", false);
        CategoryDTO dtoNullName = new CategoryDTO(1L, null, true);
        Category category = new Category();
        category.setId(1); // ID necesario para evitar NPE
        category.setName("Tablets");
        category.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            arg.setId(1); // Simula que el repo asigna un ID
            return arg;
        });
        CategoryDTO resultNullActive = categoryController.addCategory(dtoNullActive);
        assertEquals("Tablets", resultNullActive.getName());
        assertTrue(resultNullActive.getActive());
        category.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            arg.setId(1);
            return arg;
        });
        CategoryDTO resultFalseActive = categoryController.addCategory(dtoFalseActive);
        assertEquals("Tablets", resultFalseActive.getName());
        assertFalse(resultFalseActive.getActive());
        category.setName(null);
        category.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            arg.setId(1);
            return arg;
        });
        CategoryDTO resultNullName = categoryController.addCategory(dtoNullName);
        assertNull(resultNullName.getName());
        assertTrue(resultNullName.getActive());
    }

//    @Test
//    void testAddBulkCategories_branchAndLambdaCoverage() {
//        // Duplicados en la lista, case-insensitive, categoría existente en base, nombre null, active null y false
//        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", true);
//        CategoryDTO dto2 = new CategoryDTO(2L, "accesorios", false);
//        CategoryDTO dto3 = new CategoryDTO(3L, "Celulares", null);
//        CategoryDTO dto4 = new CategoryDTO(4L, "CELULARES", true);
//        CategoryDTO dto5 = new CategoryDTO(5L, null, true);
//        Category existing = new Category();
//        existing.setId(1);
//        existing.setName("Accesorios");
//        existing.setActive(true);
//        when(categoryService.getAllCategories()).thenReturn(List.of(existing));
//        Category savedCelulares = new Category();
//        savedCelulares.setId(2);
//        savedCelulares.setName("Celulares");
//        savedCelulares.setActive(true);
//        Category savedNuevo = new Category();
//        savedNuevo.setId(3);
//        savedNuevo.setName("Nuevo");
//        savedNuevo.setActive(true);
//        when(categoryService.saveCategory(any(Category.class))).thenReturn(savedCelulares, savedNuevo);
//        when(categoryService.getAllCategories()).thenReturn(List.of(existing, savedCelulares, savedNuevo));
//        List<CategoryDTO> result = categoryController.addBulkCategories(List.of(dto1, dto2, dto3, dto4, dto5));
//        assertEquals(2, result.size());
//        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Accesorios")));
//        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Celulares")));
//    }

    @Test
    void testUpdateCategory_branchAndLambdaCoverage() {
        // active null, active false, name null
        CategoryDTO dtoNullActive = new CategoryDTO(1L, "Tablets", null);
        CategoryDTO dtoFalseActive = new CategoryDTO(1L, "Tablets", false);
        CategoryDTO dtoNullName = new CategoryDTO(1L, null, true);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        // null active
        CategoryDTO resultNullActive = categoryController.updateCategory(1, dtoNullActive);
        assertEquals("Tablets", resultNullActive.getName());
        assertTrue(resultNullActive.getActive());
        // false active
        category.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO resultFalseActive = categoryController.updateCategory(1, dtoFalseActive);
        assertEquals("Tablets", resultFalseActive.getName());
        assertFalse(resultFalseActive.getActive());
        // null name
        category.setName(null);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO resultNullName = categoryController.updateCategory(1, dtoNullName);
        assertNull(resultNullName.getName());
    }

    @Test
    void testAddCategory_branchAndLambdaCoverage() {
        // active null, active false, name null
        CategoryDTO dtoNullActive = new CategoryDTO(1L, "Tablets", null);
        CategoryDTO dtoFalseActive = new CategoryDTO(1L, "Tablets", false);
        CategoryDTO dtoNullName = new CategoryDTO(1L, null, true);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            arg.setId(1);
            return arg;
        });
        CategoryDTO resultNullActive = categoryController.addCategory(dtoNullActive);
        assertEquals("Tablets", resultNullActive.getName());
        assertTrue(resultNullActive.getActive());
        category.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            arg.setId(1);
            return arg;
        });
        CategoryDTO resultFalseActive = categoryController.addCategory(dtoFalseActive);
        assertEquals("Tablets", resultFalseActive.getName());
        assertFalse(resultFalseActive.getActive());
        category.setName(null);
        category.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            arg.setId(1);
            return arg;
        });
        CategoryDTO resultNullName = categoryController.addCategory(dtoNullName);
        assertNull(resultNullName.getName());
        assertTrue(resultNullName.getActive());
    }

    @Test
    void testSyncCategoriesFromMock_EmptyMock() {
        KafkaMockService.CategorySyncMessage emptyMessage = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of()), "2025-09-02T08:29:02.072020100");
        when(kafkaMockService.getCategoriesMock()).thenReturn(emptyMessage);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        List<CategoryDTO> result = categoryController.syncCategoriesFromMock();
        assertTrue(result.isEmpty());
    }

    @Test
    void testSyncCategoriesFromMock_AllExist() {
        CategoryDTO mockCategory1 = new CategoryDTO(1L, "Celulares", true);
        KafkaMockService.CategorySyncMessage message = new KafkaMockService.CategorySyncMessage("CategorySync", new KafkaMockService.CategorySyncPayload(List.of(mockCategory1)), "2025-09-02T08:29:02.072020100");
        Category existingCategory = new Category();
        existingCategory.setId(1);
        existingCategory.setName("Celulares");
        existingCategory.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(existingCategory));
        when(kafkaMockService.getCategoriesMock()).thenReturn(message);
        List<CategoryDTO> result = categoryController.syncCategoriesFromMock();
        assertEquals(1, result.size());
        assertEquals("Celulares", result.get(0).getName());
    }
}
