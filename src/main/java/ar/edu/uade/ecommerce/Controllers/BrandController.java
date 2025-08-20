package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.BrandDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private KafkaMockService kafkaMockService;

    @GetMapping
    public List<BrandDTO> getAllBrands() {
        return kafkaMockService.getBrandsMock();
    }
}
