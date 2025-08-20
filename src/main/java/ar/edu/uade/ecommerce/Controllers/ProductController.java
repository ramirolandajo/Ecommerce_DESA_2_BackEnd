package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.ProductDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private KafkaMockService kafkaMockService;

    // Devuelve todos los productos mockeados
    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return kafkaMockService.getProductsMock();
    }

    // Ejemplo: filtrar por categoría
    @GetMapping("/category/{categoryId}")
    public List<ProductDTO> getProductsByCategory(@PathVariable Long categoryId) {
        return kafkaMockService.getProductsMock().stream()
                .filter(p -> p.getCategory().getId().equals(categoryId))
                .toList();
    }

    // Ejemplo: filtrar por marca
    @GetMapping("/brand/{brandId}")
    public List<ProductDTO> getProductsByBrand(@PathVariable Long brandId) {
        return kafkaMockService.getProductsMock().stream()
                .filter(p -> p.getBrand().getId().equals(brandId))
                .toList();
    }
}
