package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.BrandDTO;
import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public List<BrandDTO> getAllBrands() {
        // Ajustado para incluir el campo active en los DTOs (los tests lo esperan)
        return brandService.getAllBrands().stream()
                .map(b -> new BrandDTO(Long.valueOf(b.getId()), b.getName(), b.isActive()))
                .collect(Collectors.toList());
    }

    // Endpoints mock deshabilitados: la sincronización de marcas debe llegar desde la API de Comunicación/Core
    @GetMapping("/sync")
    public List<BrandDTO> syncBrandsFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint de sincronización mock deshabilitado. La sincronización debe venir desde la API de Comunicación (Core).");
    }

    @PostMapping("/mock/add")
    public BrandDTO addBrandFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use la API de Comunicación para sincronizar marcas.");
    }

    @PatchMapping("/mock/activate")
    public BrandDTO activateBrandFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use la API de Comunicación para sincronizar marcas.");
    }

    @PatchMapping("/mock/deactivate")
    public BrandDTO deactivateBrandFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use la API de Comunicación para sincronizar marcas.");
    }

    @PatchMapping("/mock/update")
    public BrandDTO updateBrandFromMock() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Endpoint mock deshabilitado. Use la API de Comunicación para sincronizar marcas.");
    }

    // Métodos reales para CRUD de marcas
    @PostMapping("/add")
    public BrandDTO addBrand(@RequestBody BrandDTO dto) {
        Brand existing = brandService.getAllBrands().stream()
                .filter(b -> (b.getName() == null && dto.getName() == null) ||
                             (b.getName() != null && dto.getName() != null && b.getName().equalsIgnoreCase(dto.getName())))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            return new BrandDTO(Long.valueOf(existing.getId()), existing.getName(), existing.isActive());
        }
        Brand brand = new Brand();
        brand.setName(dto.getName());
        brand.setActive(dto.getActive() != null ? dto.getActive() : true);
        Brand saved = brandService.saveBrand(brand);
        return new BrandDTO(Long.valueOf(saved.getId()), saved.getName(), saved.isActive());
    }

    @PatchMapping("/activate/{id}")
    public BrandDTO activateBrand(@PathVariable int id) {
        Brand brand = brandService.getAllBrands().stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        brand.setActive(true);
        Brand updated = brandService.saveBrand(brand);
        return new BrandDTO(Long.valueOf(updated.getId()), updated.getName(), updated.isActive());
    }

    @DeleteMapping("/{id}")
    public List<BrandDTO> deleteBrand(@PathVariable int id) {
        Brand brand = brandService.getAllBrands().stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        brand.setActive(false);
        brandService.saveBrand(brand);
        return brandService.getAllBrands().stream()
                .map(b -> new BrandDTO(Long.valueOf(b.getId()), b.getName(), b.isActive()))
                .collect(Collectors.toList());
    }

    @PatchMapping("/{id}")
    public BrandDTO updateBrand(@PathVariable int id, @RequestBody BrandDTO dto) {
        Brand brand = brandService.getAllBrands().stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        if (dto.getName() != null) {
            brand.setName(dto.getName());
        } else {
            brand.setName(null);
        }
        if (dto.getActive() != null) {
            brand.setActive(dto.getActive());
        }
        Brand updated = brandService.saveBrand(brand);
        return new BrandDTO(Long.valueOf(updated.getId()), updated.getName(), updated.isActive());
    }
}
