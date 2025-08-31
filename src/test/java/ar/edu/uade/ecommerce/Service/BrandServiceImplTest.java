package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BrandServiceImplTest {
    @Mock
    private BrandRepository brandRepository;
    @InjectMocks
    private BrandServiceImpl brandService;

    @Test
    void testSaveAllBrands_updateCreateDelete() {
        Brand b1 = new Brand(); b1.setId(1); b1.setName("Nike"); b1.setActive(true);
        Brand b2 = new Brand(); b2.setId(2); b2.setName("Adidas"); b2.setActive(true);
        Brand b3 = new Brand(); b3.setId(3); b3.setName("Puma"); b3.setActive(true);
        List<Brand> existing = Arrays.asList(b1, b2, b3);
        Brand incoming1 = new Brand(); incoming1.setName("Nike");
        Brand incoming2 = new Brand(); incoming2.setName("Reebok");
        List<Brand> incoming = Arrays.asList(incoming1, incoming2);
        when(brandRepository.findAll()).thenReturn(existing).thenReturn(Arrays.asList(b1, incoming2));
        when(brandRepository.findByName("Nike")).thenReturn(Optional.of(b1));
        when(brandRepository.findByName("Reebok")).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> inv.getArgument(0));
        List<Brand> result = brandService.saveAllBrands(incoming);
        assertNotNull(result);
        verify(brandRepository).delete(b2);
        verify(brandRepository).delete(b3);
        verify(brandRepository).save(b1);
        verify(brandRepository).save(incoming2);
    }

    @Test
    void testSaveAllBrands_noChanges() {
        Brand b1 = new Brand(); b1.setId(1); b1.setName("Nike"); b1.setActive(true);
        List<Brand> existing = Collections.singletonList(b1);
        List<Brand> incoming = Collections.singletonList(b1);
        when(brandRepository.findAll()).thenReturn(existing).thenReturn(existing);
        when(brandRepository.findByName("Nike")).thenReturn(Optional.of(b1));
        when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> inv.getArgument(0));
        List<Brand> result = brandService.saveAllBrands(incoming);
        assertEquals(1, result.size());
        verify(brandRepository, never()).delete(any());
        verify(brandRepository).save(b1);
    }

    @Test
    void testSaveBrand_nullBrand() {
        Brand result = brandService.saveBrand(null);
        assertNull(result);
        verify(brandRepository, never()).save(any());
    }

    @Test
    void testSaveBrand_nullName() {
        Brand brand = new Brand();
        brand.setName(null);
        Brand result = brandService.saveBrand(brand);
        assertNull(result);
        verify(brandRepository, never()).save(any());
    }

    @Test
    void testSaveBrand_activeFalseWithId() {
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Nike");
        brand.setActive(false);
        when(brandRepository.save(brand)).thenReturn(brand);
        Brand result = brandService.saveBrand(brand);
        assertFalse(result.isActive());
        verify(brandRepository).save(brand);
    }

    @Test
    void testSaveBrand_activeTrueOrNull() {
        Brand brand = new Brand();
        brand.setName("Nike");
        brand.setActive(true);
        when(brandRepository.save(brand)).thenReturn(brand);
        Brand result = brandService.saveBrand(brand);
        assertTrue(result.isActive());
        verify(brandRepository).save(brand);
    }

    @Test
    void testGetAllBrands() {
        Brand b1 = new Brand(); b1.setName("Nike");
        when(brandRepository.findAll()).thenReturn(Collections.singletonList(b1));
        List<Brand> result = brandService.getAllBrands();
        assertEquals(1, result.size());
        assertEquals("Nike", result.get(0).getName());
        verify(brandRepository).findAll();
    }

    @Test
    void testDeleteAllBrands() {
        brandService.deleteAllBrands();
        verify(brandRepository).deleteAll();
    }

    @Test
    void testSaveBrand_activeFalseWithNullId() {
        Brand brand = new Brand();
        brand.setId(null);
        brand.setName("Nike");
        brand.setActive(false);
        when(brandRepository.save(brand)).thenReturn(brand);
        Brand result = brandService.saveBrand(brand);
        assertTrue(result.isActive());
        verify(brandRepository).save(brand);
    }
}
