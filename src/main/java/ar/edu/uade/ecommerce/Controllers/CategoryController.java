package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.CategoryDTO;
import ar.edu.uade.ecommerce.KafkaCommunication.KafkaMockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private KafkaMockService kafkaMockService;

    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return kafkaMockService.getCategoriesMock();
    }
}
