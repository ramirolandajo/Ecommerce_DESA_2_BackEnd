package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Brand;
import ar.edu.uade.ecommerce.Repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandRepository brandRepository;

    @Override
    public List<Brand> saveAllBrands(List<Brand> brands) {
        List<Brand> existingBrands = brandRepository.findAll();
        // Eliminar marcas que ya no están en el mock
        List<String> incomingNames = brands.stream().map(Brand::getName).collect(Collectors.toList());
        existingBrands.stream()
            .filter(b -> !incomingNames.contains(b.getName()))
            .forEach(brandRepository::delete);
        // Actualizar o crear marcas
        for (Brand incoming : brands) {
            Optional<Brand> existing = brandRepository.findByName(incoming.getName());
            if (existing.isPresent()) {
                Brand b = existing.get();
                b.setName(incoming.getName());
                b.setActive(true); // Siempre true al actualizar
                brandRepository.save(b);
            } else {
                incoming.setActive(true); // Siempre true al crear
                brandRepository.save(incoming);
            }
        }
        return brandRepository.findAll();
    }

    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Override
    public void deleteAllBrands() {
        brandRepository.deleteAll();
    }

    @Override
    public Brand saveBrand(Brand brand) {
        // Si el valor es null, setear true
        if (brand == null || brand.getName() == null) {
            return null;
        }
        // Si el valor es null (por conversión de DTO), setear true
        // El boolean primitivo nunca puede ser null
        // Si el valor es false, mantenerlo (baja lógica)
        // Si el valor es true, mantenerlo
        // Si el valor es null (no debería pasar), setear true
        // Esto es redundante pero asegura que nunca se asigne null
        // Si el valor es null, setear true
        if (!brand.isActive() && brand.getId() != null) {
            brand.setActive(false);
        } else {
            brand.setActive(true);
        }
        return brandRepository.save(brand);
    }

    @Override
    public Collection<Object> getAllActiveBrands() {
        return brandRepository.findByActiveTrue().stream()
                .map(b -> {
                    java.util.Map<String, Object> dto = new java.util.HashMap<>();
                    dto.put("name", b.getName());
                    dto.put("brandCode", b.getBrandCode());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
