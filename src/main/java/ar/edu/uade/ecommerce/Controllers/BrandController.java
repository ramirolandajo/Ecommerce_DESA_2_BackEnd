package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.BrandDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private KafkaMockService kafkaMockService;

    @Autowired
    private BrandService brandService;

    @GetMapping("/sync")
    public List<BrandDTO> syncBrandsFromMock() {
        KafkaMockService.BrandSyncMessage message = kafkaMockService.getBrandsMock();
        List<BrandDTO> mockBrands = message.payload.brands;
        List<Brand> existingBrands = brandService.getAllBrands();
        for (BrandDTO dto : mockBrands) {
            Brand existing = existingBrands.stream()
                .filter(b -> b.getName() != null && b.getName().equalsIgnoreCase(dto.getName()))
                .findFirst()
                .orElse(null);
            if (existing == null) {
                Brand b = new Brand();
                b.setName(dto.getName());
                b.setActive(dto.getActive() != null ? dto.getActive() : true);
                brandService.saveBrand(b);
            }
        }
        // Imprimir el mensaje recibido del mock en formato core de mensajería
        System.out.println("Mensaje recibido del core de mensajería:");
        System.out.println("{" +
            "type='" + message.type + "', " +
            "payload=" + message.payload + ", " +
            "timestamp=" + message.timestamp +
            "}");
        return brandService.getAllBrands().stream()
                .map(b -> new BrandDTO(Long.valueOf(b.getId()), b.getName(), b.isActive()))
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<BrandDTO> getAllBrands() {
        return brandService.getAllBrands().stream()
                .map(b -> new BrandDTO(Long.valueOf(b.getId()), b.getName()))
                .collect(Collectors.toList());
    }

    @PostMapping
    public BrandDTO addBrand(@RequestBody BrandDTO brandDTO) {
        // Verificar si ya existe una marca con el mismo nombre (incluyendo ambos null)
        Brand existing = brandService.getAllBrands().stream()
            .filter(b -> (b.getName() == null && brandDTO.getName() == null) ||
                         (b.getName() != null && b.getName().equalsIgnoreCase(brandDTO.getName())))
            .findFirst()
            .orElse(null);
        if (existing != null) {
            // Si existe, devolver el DTO de la marca existente
            return new BrandDTO(Long.valueOf(existing.getId()), existing.getName(), existing.isActive());
        }
        Brand brand = new Brand();
        brand.setName(brandDTO.getName());
        brand.setActive(brandDTO.getActive() != null ? brandDTO.getActive() : true);
        Brand saved = brandService.saveBrand(brand);
        return new BrandDTO(Long.valueOf(saved.getId()), saved.getName(), saved.isActive());
    }

    @PatchMapping("/{id}")
    public BrandDTO updateBrand(@PathVariable Integer id, @RequestBody BrandDTO brandDTO) {
        Brand brand = brandService.getAllBrands().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        brand.setName(brandDTO.getName()); // Permitir null
        if (brandDTO.getActive() != null) {
            brand.setActive(brandDTO.getActive());
        }
        brandService.saveBrand(brand);
        return new BrandDTO(Long.valueOf(brand.getId()), brand.getName(), brand.isActive());
    }

    @DeleteMapping("/{id}")
    public List<BrandDTO> deleteBrand(@PathVariable Integer id) {
        Brand brand = brandService.getAllBrands().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        brand.setActive(false);
        brandService.saveBrand(brand);
        // Devuelve el listado completo con el campo active
        return brandService.getAllBrands().stream()
                .map(b -> new BrandDTO(Long.valueOf(b.getId()), b.getName(), b.isActive()))
                .collect(java.util.stream.Collectors.toList());
    }

    @PatchMapping("/{id}/activate")
    public BrandDTO activateBrand(@PathVariable Integer id) {
        Brand brand = brandService.getAllBrands().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        brand.setActive(true);
        Brand updated = brandService.saveBrand(brand);
        return new BrandDTO(Long.valueOf(updated.getId()), updated.getName(), updated.isActive());
    }
}
