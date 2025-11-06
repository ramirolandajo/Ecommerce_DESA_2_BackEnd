package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Repository.BrandRepository;
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
class BrandServiceImplNewTests {
    @Mock BrandRepository brandRepository;
    @InjectMocks BrandServiceImpl service;

    @Test
    void saveBrand_nullOrNameNull_returnsNull() {
        assertNull(service.saveBrand(null));
        Brand b = new Brand();
        b.setName(null);
        assertNull(service.saveBrand(b));
        verifyNoInteractions(brandRepository);
    }

    @Test
    void saveBrand_setsActiveTrueForNew() {
        Brand b = new Brand();
        b.setName("ACME");
        when(brandRepository.save(b)).thenReturn(b);
        Brand out = service.saveBrand(b);
        assertTrue(out.isActive());
        verify(brandRepository).save(b);
    }

    @Test
    void saveAllBrands_whenIncomingContainsExisting_updatesAndCreates_withoutDelete() {
        Brand existing = new Brand(); existing.setId(1); existing.setName("OLD"); existing.setActive(true);
        Brand incoming1 = new Brand(); incoming1.setName("NEW1");
        Brand incoming2 = new Brand(); incoming2.setName("OLD"); // existente, no se borra

        when(brandRepository.findAll()).thenReturn(List.of(existing), List.of(incoming1, existing));
        when(brandRepository.findByName("NEW1")).thenReturn(Optional.empty());
        when(brandRepository.findByName("OLD")).thenReturn(Optional.of(existing));
        when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Brand> result = service.saveAllBrands(List.of(incoming1, incoming2));
        assertEquals(2, result.size());
        verify(brandRepository, never()).delete(any());
        verify(brandRepository, atLeastOnce()).save(any(Brand.class));
    }

    @Test
    void saveAllBrands_whenIncomingDoesNotContainExisting_deletesMissing() {
        Brand existing = new Brand(); existing.setId(1); existing.setName("OLD"); existing.setActive(true);
        Brand incoming1 = new Brand(); incoming1.setName("NEW1");

        when(brandRepository.findAll()).thenReturn(List.of(existing), List.of(incoming1));
        when(brandRepository.findByName("NEW1")).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Brand> result = service.saveAllBrands(List.of(incoming1));
        assertEquals(1, result.size());
        verify(brandRepository).delete(existing);
        verify(brandRepository, atLeastOnce()).save(any(Brand.class));
    }

    @Test
    void getAllActiveBrands_mapsToDTO() {
        Brand b = new Brand(); b.setId(5); b.setName("N"); b.setBrandCode(123); b.setActive(true);
        when(brandRepository.findByActiveTrue()).thenReturn(List.of(b));
        var out = service.getAllActiveBrands();
        assertEquals(1, out.size());
        @SuppressWarnings("unchecked")
        var map = (java.util.Map<String,Object>) out.iterator().next();
        assertEquals(5, map.get("id"));
        assertEquals("N", map.get("name"));
        assertEquals(123, map.get("brandCode"));
    }
}
