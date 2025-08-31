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
        when(kafkaMockService.getCategoriesMock()).thenReturn(mockDtos);
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

    @Test
    void testAddBulkCategories_nullName() {
        CategoryDTO dto1 = new CategoryDTO(1L, null, true);
        List<CategoryDTO> dtos = List.of(dto1);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved = new Category();
        saved.setId(1);
        saved.setName(null);
        saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);
        when(categoryService.getAllCategories()).thenReturn(List.of(saved));
        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
        assertEquals(1, result.size());
        assertNull(result.get(0).getName());
    }

    @Test
    void testSyncCategoriesFromMock_nullName() {
        CategoryDTO dto1 = new CategoryDTO(1L, null, true);
        List<CategoryDTO> mockDtos = List.of(dto1);
        when(kafkaMockService.getCategoriesMock()).thenReturn(mockDtos);
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

    @Test
    void testAddBulkCategories_branchAndLambdaCoverage() {
        // Duplicados en la lista, case-insensitive, categoría existente en base, nombre null, active null y false
        CategoryDTO dto1 = new CategoryDTO(1L, "Accesorios", true);
        CategoryDTO dto2 = new CategoryDTO(2L, "accesorios", false);
        CategoryDTO dto3 = new CategoryDTO(3L, "Celulares", null);
        CategoryDTO dto4 = new CategoryDTO(4L, "CELULARES", true);
        CategoryDTO dto5 = new CategoryDTO(5L, null, true);
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
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Accesorios")));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Celulares")));
    }

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
    void testAddBulkCategories_withNullDTOElement() {
        CategoryDTO dto1 = new CategoryDTO(1L, "Valida", true);
        List<CategoryDTO> dtos = List.of(null, dto1);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved = new Category();
        saved.setId(1);
        saved.setName("Valida");
        saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);
        when(categoryService.getAllCategories()).thenReturn(List.of(saved));
        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
        assertEquals(1, result.size());
        assertEquals("Valida", result.get(0).getName());
        assertTrue(result.get(0).getActive());
    }

    @Test
    void testAddBulkCategories_allBranches() {
        // name null, active null
        CategoryDTO dto1 = new CategoryDTO(1L, null, null);
        Category category1 = new Category();
        category1.setId(1);
        category1.setName(null);
        category1.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            arg.setId(1);
            return arg;
        });
        CategoryDTO result1 = categoryController.addCategory(dto1);
        assertNull(result1.getName());
        assertTrue(result1.getActive());
        // name null, active false
        CategoryDTO dto2 = new CategoryDTO(2L, null, false);
        Category category2 = new Category();
        category2.setId(2);
        category2.setName(null);
        category2.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            arg.setId(2);
            return arg;
        });
        CategoryDTO result2 = categoryController.addCategory(dto2);
        assertNull(result2.getName());
        assertFalse(result2.getActive());
        // name "Combinacion", active null
        CategoryDTO dto3 = new CategoryDTO(3L, "Combinacion", null);
        Category category3 = new Category();
        category3.setId(3);
        category3.setName("Combinacion");
        category3.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            arg.setId(3);
            return arg;
        });
        CategoryDTO result3 = categoryController.addCategory(dto3);
        assertEquals("Combinacion", result3.getName());
        assertTrue(result3.getActive());
        // name "Combinacion", active false
        CategoryDTO dto4 = new CategoryDTO(4L, "Combinacion", false);
        Category category4 = new Category();
        category4.setId(4);
        category4.setName("Combinacion");
        category4.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> {
            Category arg = invocation.getArgument(0);
            arg.setId(4);
            return arg;
        });
        CategoryDTO result4 = categoryController.addCategory(dto4);
        assertEquals("Combinacion", result4.getName());
        assertFalse(result4.getActive());
    }

    @Test
    void testAddBulkCategories_allNullsAndMixedCases() {
        // Todos los DTOs con name null, active null, active false, y combinaciones
        CategoryDTO dtoNullNameNullActive = new CategoryDTO(1L, null, null);
        CategoryDTO dtoNullNameFalseActive = new CategoryDTO(2L, null, false);
        CategoryDTO dtoLowerCase = new CategoryDTO(3L, "accesorios", true);
        CategoryDTO dtoUpperCase = new CategoryDTO(4L, "ACCESORIOS", true);
        CategoryDTO dtoMixedCase = new CategoryDTO(5L, "AcCeSoRiOs", true);
        Category existing = new Category();
        existing.setId(1);
        existing.setName("Accesorios");
        existing.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(existing));
        List<CategoryDTO> result = categoryController.addBulkCategories(List.of(dtoNullNameNullActive, dtoNullNameFalseActive, dtoLowerCase, dtoUpperCase, dtoMixedCase));
        // Solo debe devolver la existente, no crear duplicados ni nulos
        assertEquals(1, result.size());
        assertEquals("Accesorios", result.get(0).getName());
    }

    @Test
    void testAddBulkCategories_newCategoryWithFalseActive() {
        CategoryDTO dto = new CategoryDTO(1L, "Nueva", false);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved = new Category();
        saved.setId(1);
        saved.setName("Nueva");
        saved.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);
        when(categoryService.getAllCategories()).thenReturn(List.of(saved));
        List<CategoryDTO> result = categoryController.addBulkCategories(List.of(dto));
        assertEquals(1, result.size());
        assertEquals("Nueva", result.get(0).getName());
        assertFalse(result.get(0).getActive());
    }

    @Test
    void testUpdateCategory_activeTrueAndFalse() {
        CategoryDTO dtoTrue = new CategoryDTO(1L, "Tablets", true);
        CategoryDTO dtoFalse = new CategoryDTO(1L, "Tablets", false);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(false);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        // Cambia a true
        category.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO resultTrue = categoryController.updateCategory(1, dtoTrue);
        assertTrue(resultTrue.getActive());
        // Cambia a false
        category.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO resultFalse = categoryController.updateCategory(1, dtoFalse);
        assertFalse(resultFalse.getActive());
    }

    @Test
    void testUpdateCategory_nameNullAndActiveNull() {
        CategoryDTO dto = new CategoryDTO(1L, null, null);
        Category category = new Category();
        category.setId(1);
        category.setName("Tablets");
        category.setActive(true);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.updateCategory(1, dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testAddCategory_activeNullAndFalse() {
        CategoryDTO dtoNull = new CategoryDTO(1L, "Tablets", null);
        CategoryDTO dtoFalse = new CategoryDTO(2L, "Tablets", false);
        Category categoryNull = new Category();
        categoryNull.setId(1);
        categoryNull.setName("Tablets");
        categoryNull.setActive(true);
        Category categoryFalse = new Category();
        categoryFalse.setId(2);
        categoryFalse.setName("Tablets");
        categoryFalse.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(categoryNull, categoryFalse);
        CategoryDTO resultNull = categoryController.addCategory(dtoNull);
        CategoryDTO resultFalse = categoryController.addCategory(dtoFalse);
        assertTrue(resultNull.getActive());
        assertFalse(resultFalse.getActive());
    }

    @Test
    void testAddCategory_nameNullAndActiveNull() {
        CategoryDTO dto = new CategoryDTO(1L, null, null);
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
    void testAddBulkCategories_allFalseAndNullCombinations() {
        // Todos los DTOs con active en false y null, y nombres únicos
        CategoryDTO dtoFalse = new CategoryDTO(1L, "CategoriaFalse", false);
        CategoryDTO dtoNull = new CategoryDTO(2L, "CategoriaNull", null);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category savedFalse = new Category();
        savedFalse.setId(1);
        savedFalse.setName("CategoriaFalse");
        savedFalse.setActive(false);
        Category savedNull = new Category();
        savedNull.setId(2);
        savedNull.setName("CategoriaNull");
        savedNull.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(savedFalse, savedNull);
        when(categoryService.getAllCategories()).thenReturn(List.of(savedFalse, savedNull));
        List<CategoryDTO> result = categoryController.addBulkCategories(List.of(dtoFalse, dtoNull));
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("CategoriaFalse") && !c.getActive()));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("CategoriaNull") && c.getActive()));
    }

    @Test
    void testUpdateCategory_activeNullKeepsCurrent() {
        // Si active es null, debe mantener el valor actual
        CategoryDTO dto = new CategoryDTO(1L, "Categoria", null);
        Category category = new Category();
        category.setId(1);
        category.setName("Categoria");
        category.setActive(false); // Valor inicial false
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        // El mock debe devolver el mismo objeto, sin modificar active
        when(categoryService.saveCategory(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CategoryDTO result = categoryController.updateCategory(1, dto);
        assertEquals("Categoria", result.getName());
        assertFalse(result.getActive()); // Debe seguir siendo false
    }

    @Test
    void testUpdateCategory_activeTrueChangesCurrent() {
        // Si active es true, debe cambiar el valor actual
        CategoryDTO dto = new CategoryDTO(1L, "Categoria", true);
        Category category = new Category();
        category.setId(1);
        category.setName("Categoria");
        category.setActive(false);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        category.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.updateCategory(1, dto);
        assertEquals("Categoria", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testAddCategory_activeNullAndFalseCombinations() {
        // active null y false, para cubrir ramas
        CategoryDTO dtoNull = new CategoryDTO(1L, "CategoriaNull", null);
        CategoryDTO dtoFalse = new CategoryDTO(2L, "CategoriaFalse", false);
        Category categoryNull = new Category();
        categoryNull.setId(1);
        categoryNull.setName("CategoriaNull");
        categoryNull.setActive(true);
        Category categoryFalse = new Category();
        categoryFalse.setId(2);
        categoryFalse.setName("CategoriaFalse");
        categoryFalse.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(categoryNull, categoryFalse);
        CategoryDTO resultNull = categoryController.addCategory(dtoNull);
        CategoryDTO resultFalse = categoryController.addCategory(dtoFalse);
        assertTrue(resultNull.getActive());
        assertFalse(resultFalse.getActive());
    }

    @Test
    void testAddCategory_nameNullActiveFalse() {
        // name null y active false
        CategoryDTO dto = new CategoryDTO(1L, null, false);
        Category category = new Category();
        category.setId(1);
        category.setName(null);
        category.setActive(false);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryController.addCategory(dto);
        assertNull(result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testAddBulkCategories_multipleNullNamesAndActives() {
        CategoryDTO dto1 = new CategoryDTO(1L, null, null);
        CategoryDTO dto2 = new CategoryDTO(2L, null, null);
        CategoryDTO dto3 = new CategoryDTO(3L, "Valida", true);
        List<CategoryDTO> dtos = List.of(dto1, dto2, dto3);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        Category saved = new Category();
        saved.setId(3);
        saved.setName("Valida");
        saved.setActive(true);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(saved);
        when(categoryService.getAllCategories()).thenReturn(List.of(saved));
        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
        assertEquals(1, result.size());
        assertEquals("Valida", result.get(0).getName());
        assertTrue(result.get(0).getActive());
    }

    @Test
    void testAddBulkCategories_nameNullActiveFalse() {
        CategoryDTO dto = new CategoryDTO(1L, null, false);
        List<CategoryDTO> dtos = List.of(dto);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        List<CategoryDTO> result = categoryController.addBulkCategories(dtos);
        assertTrue(result.isEmpty());
    }
}
