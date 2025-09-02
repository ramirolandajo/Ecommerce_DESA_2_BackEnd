package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.BrandDTO;
import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Service.BrandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrandControllerTest {
    @Mock
    private KafkaMockService kafkaMockService;
    @Mock
    private BrandService brandService;
    @InjectMocks
    private BrandController brandController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ...existing tests...

    @Test
    void testAddBrand_NullName() {
        BrandDTO dto = new BrandDTO(1L, null, true);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName(null);
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testAddBrand_NullActive() {
        BrandDTO dto = new BrandDTO(1L, "Xiaomi", null);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Xiaomi");
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("Xiaomi", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testGetAllBrands_EmptyList() {
        when(brandService.getAllBrands()).thenReturn(List.of());
        List<BrandDTO> result = brandController.getAllBrands();
        assertTrue(result.isEmpty());
    }

    @Test
    void testSyncBrandsFromMock_EmptyMock() {
        KafkaMockService.BrandSyncMessage emptyMessage = new KafkaMockService.BrandSyncMessage("BrandSync", new KafkaMockService.BrandSyncPayload(List.of()), "2025-09-02T08:29:02.072020100");
        when(kafkaMockService.getBrandsMock()).thenReturn(emptyMessage);
        when(brandService.getAllBrands()).thenReturn(List.of());
        List<BrandDTO> result = brandController.syncBrandsFromMock();
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateBrand_NullActive() {
        BrandDTO dto = new BrandDTO(1L, "Sony", null);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.updateBrand(1, dto);
        assertEquals("Sony", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testDeleteBrand_EmptyList() {
        when(brandService.getAllBrands()).thenReturn(List.of());
        assertThrows(RuntimeException.class, () -> brandController.deleteBrand(1));
    }

    @Test
    void testActivateBrand_EmptyList() {
        when(brandService.getAllBrands()).thenReturn(List.of());
        assertThrows(RuntimeException.class, () -> brandController.activateBrand(1));
    }

    @Test
    void testSyncBrandsFromMock_AllExist() {
        BrandDTO mockBrand1 = new BrandDTO(1L, "Apple", true);
        KafkaMockService.BrandSyncMessage message = new KafkaMockService.BrandSyncMessage("BrandSync", new KafkaMockService.BrandSyncPayload(List.of(mockBrand1)), "2025-09-02T08:29:02.072020100");
        Brand existingBrand = new Brand();
        existingBrand.setId(1);
        existingBrand.setName("Apple");
        existingBrand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(existingBrand));
        when(kafkaMockService.getBrandsMock()).thenReturn(message);
        List<BrandDTO> result = brandController.syncBrandsFromMock();
        assertEquals(1, result.size());
        assertEquals("Apple", result.get(0).getName());
    }

    @Test
    void testAddBrand_AllFieldsNull() {
        BrandDTO dto = new BrandDTO(null, null, null);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName(null);
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testUpdateBrand_NotFound() {
        BrandDTO dto = new BrandDTO(99L, "NoExiste", true);
        when(brandService.getAllBrands()).thenReturn(List.of());
        assertThrows(RuntimeException.class, () -> brandController.updateBrand(99, dto));
    }

    @Test
    void testUpdateBrand_ActiveNotNull() {
        BrandDTO dto = new BrandDTO(1L, "Sony", false);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.updateBrand(1, dto);
        assertEquals("Sony", result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testAddBrand_AlreadyExists() {
        BrandDTO dto = new BrandDTO(1L, "Apple", true);
        Brand existingBrand = new Brand();
        existingBrand.setId(1);
        existingBrand.setName("Apple");
        existingBrand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(existingBrand));
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("Apple", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testAddBrand_ActiveNotNull() {
        BrandDTO dto = new BrandDTO(2L, "Samsung", false);
        Brand brand = new Brand();
        brand.setId(2);
        brand.setName("Samsung");
        brand.setActive(false);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        when(brandService.getAllBrands()).thenReturn(List.of());
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("Samsung", result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testUpdateBrand_ActiveTrue() {
        BrandDTO dto = new BrandDTO(1L, "Sony", true);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(false);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.updateBrand(1, dto);
        assertEquals("Sony", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testUpdateBrand_ActiveFalse() {
        BrandDTO dto = new BrandDTO(1L, "Sony", false);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.updateBrand(1, dto);
        assertEquals("Sony", result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testUpdateBrand_NullName() {
        BrandDTO dto = new BrandDTO(1L, null, true);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.updateBrand(1, dto);
        assertNull(result.getName());
    }

    @Test
    void testAddBrand_NameNull() {
        BrandDTO dto = new BrandDTO(1L, null, true);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName(null);
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        when(brandService.getAllBrands()).thenReturn(List.of());
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testAddBrand_ActiveTrue() {
        BrandDTO dto = new BrandDTO(2L, "Samsung", true);
        Brand brand = new Brand();
        brand.setId(2);
        brand.setName("Samsung");
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        when(brandService.getAllBrands()).thenReturn(List.of());
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("Samsung", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testAddBrand_ActiveFalse() {
        BrandDTO dto = new BrandDTO(3L, "LG", false);
        Brand brand = new Brand();
        brand.setId(3);
        brand.setName("LG");
        brand.setActive(false);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        when(brandService.getAllBrands()).thenReturn(List.of());
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("LG", result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testAddBrand_ListWithNullName() {
        BrandDTO dto = new BrandDTO(4L, "Motorola", true);
        Brand brandNull = new Brand();
        brandNull.setId(5);
        brandNull.setName(null);
        brandNull.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brandNull));
        Brand brand = new Brand();
        brand.setId(4);
        brand.setName("Motorola");
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("Motorola", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testAddBrand_EmptyList() {
        BrandDTO dto = new BrandDTO(6L, "Nokia", true);
        when(brandService.getAllBrands()).thenReturn(List.of());
        Brand brand = new Brand();
        brand.setId(6);
        brand.setName("Nokia");
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("Nokia", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testSyncBrandsFromMock_NullNameInMock_shouldNotAddNullName() {
        BrandDTO mockDto = new BrandDTO(7L, null, true);
        KafkaMockService.BrandSyncMessage message = new KafkaMockService.BrandSyncMessage("BrandSync", new KafkaMockService.BrandSyncPayload(List.of(mockDto)), "2025-09-02T08:29:02.072020100");
        when(kafkaMockService.getBrandsMock()).thenReturn(message);
        when(brandService.getAllBrands()).thenReturn(List.of());
        // El método no debería agregar marcas con nombre null
        List<BrandDTO> result = brandController.syncBrandsFromMock();
        assertTrue(result.stream().noneMatch(b -> b.getName() == null));
    }

    @Test
    void testSyncBrandsFromMock_EmptyExistingBrands_shouldAddBrand() {
        BrandDTO mockDto = new BrandDTO(8L, "Alcatel", true);
        KafkaMockService.BrandSyncMessage message = new KafkaMockService.BrandSyncMessage("BrandSync", new KafkaMockService.BrandSyncPayload(List.of(mockDto)), "2025-09-02T08:29:02.072020100");
        when(kafkaMockService.getBrandsMock()).thenReturn(message);
        when(brandService.getAllBrands()).thenReturn(List.of());
        Brand brand = new Brand();
        brand.setId(8);
        brand.setName("Alcatel");
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        // Simula que después de guardar, la marca está en la lista
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        List<BrandDTO> result = brandController.syncBrandsFromMock();
        assertTrue(result.stream().anyMatch(b -> "Alcatel".equals(b.getName())));
    }

    @Test
    void testAddBrand_NameNull_AlreadyExists() {
        BrandDTO dto = new BrandDTO(10L, null, true);
        Brand existingBrand = new Brand();
        existingBrand.setId(11);
        existingBrand.setName(null);
        existingBrand.setActive(true);
        // Solo mockeamos getAllBrands, no saveBrand
        when(brandService.getAllBrands()).thenReturn(List.of(existingBrand));
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(existingBrand.getId()), result.getId());
    }

    @Test
    void testAddBrand_NameNull_NotExists() {
        BrandDTO dto = new BrandDTO(12L, null, true);
        when(brandService.getAllBrands()).thenReturn(List.of());
        Brand brand = new Brand();
        brand.setId(12);
        brand.setName(null);
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(brand.getId()), result.getId());
    }

    @Test
    void testAddBrand_ListWithNullName_BothNull() {
        BrandDTO dto = new BrandDTO(13L, null, true);
        Brand brandNull1 = new Brand();
        brandNull1.setId(14);
        brandNull1.setName(null);
        brandNull1.setActive(true);
        Brand brandNull2 = new Brand();
        brandNull2.setId(15);
        brandNull2.setName(null);
        brandNull2.setActive(false);
        // Solo mockeamos getAllBrands, no saveBrand
        when(brandService.getAllBrands()).thenReturn(List.of(brandNull1, brandNull2));
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(brandNull1.getId()), result.getId());
    }

    @Test
    void testSyncBrandsFromMock_NullNameInMockAndExisting() {
        BrandDTO mockDto = new BrandDTO(16L, null, true);
        Brand existingBrand = new Brand();
        existingBrand.setId(17);
        existingBrand.setName(null);
        existingBrand.setActive(true);
        KafkaMockService.BrandSyncPayload payload = new KafkaMockService.BrandSyncPayload(List.of(mockDto));
        KafkaMockService.BrandSyncMessage mockMessage = new KafkaMockService.BrandSyncMessage("BrandSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getBrandsMock()).thenReturn(mockMessage);
        when(brandService.getAllBrands()).thenReturn(List.of(existingBrand));
        List<BrandDTO> result = brandController.syncBrandsFromMock();
        assertTrue(result.stream().anyMatch(b -> b.getName() == null));
    }

    @Test
    void testActivateBrand_Success() {
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(false);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.activateBrand(1);
        assertEquals("Sony", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testActivateBrand_AlreadyActive() {
        Brand brand = new Brand();
        brand.setId(2);
        brand.setName("LG");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.activateBrand(2);
        assertEquals("LG", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testActivateBrand_NotFound() {
        when(brandService.getAllBrands()).thenReturn(List.of());
        assertThrows(RuntimeException.class, () -> brandController.activateBrand(99));
    }

    @Test
    void testDeleteBrand_Success() {
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        List<BrandDTO> result = brandController.deleteBrand(1);
        assertFalse(result.get(0).getActive());
    }

    @Test
    void testDeleteBrand_AlreadyInactive() {
        Brand brand = new Brand();
        brand.setId(2);
        brand.setName("LG");
        brand.setActive(false);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        List<BrandDTO> result = brandController.deleteBrand(2);
        assertFalse(result.get(0).getActive());
    }

    @Test
    void testDeleteBrand_NotFound() {
        when(brandService.getAllBrands()).thenReturn(List.of());
        assertThrows(RuntimeException.class, () -> brandController.deleteBrand(99));
    }

    @Test
    void testGetAllBrands_NullValues() {
        Brand brand = new Brand();
        brand.setId(3);
        brand.setName(null);
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        List<BrandDTO> result = brandController.getAllBrands();
        assertNull(result.get(0).getName());
        assertTrue(result.get(0).getActive());
    }

    @Test
    void testUpdateBrand_ActiveNull_NameChange() {
        BrandDTO dto = new BrandDTO(1L, "NuevoNombre", null);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.updateBrand(1, dto);
        assertEquals("NuevoNombre", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testAddBrand_NameNull_ExistsAndActiveNull() {
        BrandDTO dto = new BrandDTO(20L, null, null);
        Brand existingBrand = new Brand();
        existingBrand.setId(21);
        existingBrand.setName(null);
        existingBrand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(existingBrand));
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(existingBrand.getId()), result.getId());
    }

    @Test
    void testAddBrand_NameNull_NotExistsAndActiveNull() {
        BrandDTO dto = new BrandDTO(22L, null, null);
        when(brandService.getAllBrands()).thenReturn(List.of());
        Brand brand = new Brand();
        brand.setId(22);
        brand.setName(null);
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(brand.getId()), result.getId());
    }

    @Test
    void testAddBrand_ActiveNull_NameNotExists() {
        BrandDTO dto = new BrandDTO(23L, "Nueva", null);
        when(brandService.getAllBrands()).thenReturn(List.of());
        Brand brand = new Brand();
        brand.setId(23);
        brand.setName("Nueva");
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("Nueva", result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(brand.getId()), result.getId());
    }

    @Test
    void testAddBrand_ActiveNull_NameExists() {
        BrandDTO dto = new BrandDTO(24L, "Apple", null);
        Brand existingBrand = new Brand();
        existingBrand.setId(25);
        existingBrand.setName("Apple");
        existingBrand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(existingBrand));
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("Apple", result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(existingBrand.getId()), result.getId());
    }

    @Test
    void testAddBrand_ListWithNullName_EmptyList() {
        BrandDTO dto = new BrandDTO(26L, null, true);
        when(brandService.getAllBrands()).thenReturn(List.of());
        Brand brand = new Brand();
        brand.setId(26);
        brand.setName(null);
        brand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(brand.getId()), result.getId());
    }

    @Test
    void testUpdateBrand_ActiveNull_NameToNull() {
        BrandDTO dto = new BrandDTO(1L, null, null);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.updateBrand(1, dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testUpdateBrand_ActiveTrue_NameNull() {
        BrandDTO dto = new BrandDTO(1L, null, true);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(false);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.updateBrand(1, dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void testUpdateBrand_ActiveFalse_NameNull() {
        BrandDTO dto = new BrandDTO(1L, null, false);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.updateBrand(1, dto);
        assertNull(result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void testAddBrand_NameNull_ListWithNonNullName() {
        BrandDTO dto = new BrandDTO(30L, null, true);
        Brand brand = new Brand();
        brand.setId(31);
        brand.setName("Apple");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        Brand newBrand = new Brand();
        newBrand.setId(32);
        newBrand.setName(null);
        newBrand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(newBrand);
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(newBrand.getId()), result.getId());
    }

    @Test
    void testAddBrand_NameNotNull_ListWithNullName() {
        BrandDTO dto = new BrandDTO(33L, "Samsung", true);
        Brand brand = new Brand();
        brand.setId(34);
        brand.setName(null);
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        Brand newBrand = new Brand();
        newBrand.setId(35);
        newBrand.setName("Samsung");
        newBrand.setActive(true);
        when(brandService.saveBrand(any(Brand.class))).thenReturn(newBrand);
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("Samsung", result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(newBrand.getId()), result.getId());
    }

    @Test
    void testAddBrand_ListWithNullAndNonNullNames() {
        BrandDTO dtoNull = new BrandDTO(36L, null, true);
        BrandDTO dtoNotNull = new BrandDTO(37L, "Motorola", true);
        Brand brandNull = new Brand();
        brandNull.setId(38);
        brandNull.setName(null);
        brandNull.setActive(true);
        Brand brandNotNull = new Brand();
        brandNotNull.setId(39);
        brandNotNull.setName("Motorola");
        brandNotNull.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brandNull, brandNotNull));
        // Para dtoNull debe devolver el existente con nombre null
        BrandDTO resultNull = brandController.addBrand(dtoNull);
        assertNull(resultNull.getName());
        assertTrue(resultNull.getActive());
        assertEquals(Long.valueOf(brandNull.getId()), resultNull.getId());
        // Para dtoNotNull debe devolver el existente con nombre "Motorola"
        BrandDTO resultNotNull = brandController.addBrand(dtoNotNull);
        assertEquals("Motorola", resultNotNull.getName());
        assertTrue(resultNotNull.getActive());
        assertEquals(Long.valueOf(brandNotNull.getId()), resultNotNull.getId());
    }

    @Test
    void testSyncBrandsFromMock_MixedNullNames() {
        BrandDTO mockDtoNull = new BrandDTO(40L, null, true);
        BrandDTO mockDtoNotNull = new BrandDTO(41L, "Sony", true);
        Brand existingNull = new Brand();
        existingNull.setId(42);
        existingNull.setName(null);
        existingNull.setActive(true);
        Brand existingNotNull = new Brand();
        existingNotNull.setId(43);
        existingNotNull.setName("Sony");
        existingNotNull.setActive(true);
        KafkaMockService.BrandSyncPayload payload = new KafkaMockService.BrandSyncPayload(List.of(mockDtoNull, mockDtoNotNull));
        KafkaMockService.BrandSyncMessage mockMessage = new KafkaMockService.BrandSyncMessage("BrandSync", payload, java.time.LocalDateTime.now().toString());
        when(kafkaMockService.getBrandsMock()).thenReturn(mockMessage);
        when(brandService.getAllBrands()).thenReturn(List.of(existingNull, existingNotNull));
        List<BrandDTO> result = brandController.syncBrandsFromMock();
        assertTrue(result.stream().anyMatch(b -> b.getName() == null));
        assertTrue(result.stream().anyMatch(b -> "Sony".equals(b.getName())));
    }

    @Test
    void testUpdateBrand_IdNotFound() {
        BrandDTO dto = new BrandDTO(99L, "NuevaMarca", true);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        assertThrows(RuntimeException.class, () -> brandController.updateBrand(99, dto));
    }

    @Test
    void testUpdateBrand_ChangeName() {
        BrandDTO dto = new BrandDTO(1L, "Panasonic", true);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenReturn(brand);
        BrandDTO result = brandController.updateBrand(1, dto);
        assertEquals("Panasonic", result.getName());
    }

    @Test
    void testUpdateBrand_ActiveNull_CurrentFalse() {
        BrandDTO dto = new BrandDTO(1L, "Sony", null);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(false);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandService.saveBrand(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BrandDTO result = brandController.updateBrand(1, dto);
        // Comparar con el valor real de la entidad Brand
        assertEquals(brand.isActive(), result.getActive());
        assertFalse(result.getActive());
    }

    @Test
    void testAddBrand_ExistingName() {
        BrandDTO dto = new BrandDTO(2L, "Sony", true);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Sony");
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        BrandDTO result = brandController.addBrand(dto);
        assertEquals("Sony", result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(1), result.getId());
    }

    @Test
    void testAddBrand_ExistingNullName() {
        BrandDTO dto = new BrandDTO(2L, null, true);
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName(null);
        brand.setActive(true);
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        BrandDTO result = brandController.addBrand(dto);
        assertNull(result.getName());
        assertTrue(result.getActive());
        assertEquals(Long.valueOf(1), result.getId());
    }
}
