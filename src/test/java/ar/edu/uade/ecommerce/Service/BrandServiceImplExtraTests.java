package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BrandServiceImplExtraTests {
    @Mock
    private BrandRepository brandRepository;
    @InjectMocks
    private BrandServiceImpl brandService;

    @Test
    void testSaveBrand_whenRepositoryThrowsException() {
        Brand brand = new Brand();
        brand.setName("Nike");
        when(brandRepository.save(brand)).thenThrow(new RuntimeException("Save error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> brandService.saveBrand(brand));
        assertEquals("Save error", ex.getMessage());
    }

    @Test
    void testGetAllBrands_whenRepositoryThrowsException() {
        when(brandRepository.findAll()).thenThrow(new RuntimeException("Find all error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> brandService.getAllBrands());
        assertEquals("Find all error", ex.getMessage());
    }

    @Test
    void testSaveAllBrands_whenFindAllThrowsException() {
        when(brandRepository.findAll()).thenThrow(new RuntimeException("Find all error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> brandService.saveAllBrands(Collections.emptyList()));
        assertEquals("Find all error", ex.getMessage());
    }

    @Test
    void testSaveAllBrands_whenFindByNameThrowsException() {
        Brand incoming = new Brand();
        incoming.setName("Nike");
        when(brandRepository.findAll()).thenReturn(Collections.emptyList());
        when(brandRepository.findByName("Nike")).thenThrow(new RuntimeException("Find by name error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> brandService.saveAllBrands(List.of(incoming)));
        assertEquals("Find by name error", ex.getMessage());
    }

    @Test
    void testDeleteAllBrands_whenRepositoryThrowsException() {
        doThrow(new RuntimeException("Delete all error")).when(brandRepository).deleteAll();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> brandService.deleteAllBrands());
        assertEquals("Delete all error", ex.getMessage());
    }

    @Test
    void testSaveBrand_withEmptyName() {
        Brand brand = new Brand();
        brand.setName("");
        Brand result = brandService.saveBrand(brand);
        assertNull(result);
    }

    @Test
    void testSaveAllBrands_whenSaveThrowsException() {
        Brand incoming = new Brand();
        incoming.setName("Nike");
        when(brandRepository.findAll()).thenReturn(Collections.emptyList());
        when(brandRepository.findByName("Nike")).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenThrow(new RuntimeException("Save error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> brandService.saveAllBrands(List.of(incoming)));
        assertEquals("Save error", ex.getMessage());
    }

    @Test
    void testSaveAllBrands_whenDeleteThrowsException() {
        Brand existing = new Brand();
        existing.setId(1);
        existing.setName("OldBrand");
        Brand incoming = new Brand();
        incoming.setName("NewBrand");
        when(brandRepository.findAll()).thenReturn(List.of(existing));
        when(brandRepository.findByName("NewBrand")).thenReturn(Optional.empty());
        when(brandRepository.findByName("OldBrand")).thenReturn(Optional.of(existing));
        when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("Delete error")).when(brandRepository).delete(existing);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> brandService.saveAllBrands(List.of(incoming)));
        assertEquals("Delete error", ex.getMessage());
    }

    @Test
    void testSaveAllBrands_withNullIncomingList() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> brandService.saveAllBrands(null));
        assertNotNull(ex);
    }
}