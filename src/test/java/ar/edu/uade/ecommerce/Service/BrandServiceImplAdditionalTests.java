package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Repository.BrandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplAdditionalTests {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandServiceImpl brandService;

    private Brand b1, b2;

    @BeforeEach
    void setUp() {
        b1 = new Brand();
        b1.setId(1);
        b1.setName("A");
        b1.setActive(true);

        b2 = new Brand();
        b2.setId(2);
        b2.setName("B");
        b2.setActive(true);
    }

    @Test
    void saveBrand_nullBrand_returnsNull() {
        assertNull(brandService.saveBrand(null));
    }

    @Test
    void saveBrand_nullName_returnsNullAndDoesNotSave() {
        Brand b = new Brand();
        b.setName(null);
        assertNull(brandService.saveBrand(b));
        verify(brandRepository, never()).save(any());
    }

    @Test
    void saveBrand_emptyName_savedWithActiveTrue() {
        Brand b = new Brand();
        b.setName("");
        when(brandRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Brand saved = brandService.saveBrand(b);
        assertNotNull(saved);
        assertTrue(saved.isActive());
        verify(brandRepository).save(saved);
    }

    @Test
    void saveAllBrands_nullIncoming_throwsNPEorHandled() {
        when(brandRepository.findAll()).thenReturn(new ArrayList<>());
        // Simular llamada con null: según implementación original fallará; aquí comprobamos que maneja la situación
        assertThrows(NullPointerException.class, () -> brandService.saveAllBrands(null));
    }

    @Test
    void saveAllBrands_removesMissingAndUpdatesExisting() {
        when(brandRepository.findAll()).thenReturn(Arrays.asList(b1, b2));
        Brand incomingA = new Brand(); incomingA.setName("A");
        Brand incomingC = new Brand(); incomingC.setName("C");

        when(brandRepository.findByName("A")).thenReturn(Optional.of(b1));
        when(brandRepository.findByName("C")).thenReturn(Optional.empty());

        when(brandRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(brandRepository.findAll()).thenReturn(Arrays.asList(b1, b2, incomingC));

        List<Brand> result = brandService.saveAllBrands(Arrays.asList(incomingA, incomingC));

        assertNotNull(result);
        // se espera que al menos la nueva y la existente estén presentes
        assertTrue(result.stream().anyMatch(b -> "A".equals(b.getName())));
        assertTrue(result.stream().anyMatch(b -> "C".equals(b.getName())));

        verify(brandRepository).delete(b2);
        verify(brandRepository, atLeast(1)).save(any());
    }
}
