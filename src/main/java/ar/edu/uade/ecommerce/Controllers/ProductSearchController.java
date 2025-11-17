package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.DTO.ProductDTO;
import ar.edu.uade.ecommerce.Entity.DTO.SearchProductDTO;
import ar.edu.uade.ecommerce.Entity.Product;
import ar.edu.uade.ecommerce.Repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products/search")
public class ProductSearchController {
    @Autowired
    private ProductRepository productRepository;

    private Map<String, List<String>> synonyms;

    public ProductSearchController() {
        loadSynonyms();
    }

    private void loadSynonyms() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String path = "src/main/resources/synonyms.json";
            String json = new String(Files.readAllBytes(Paths.get(path)));
            synonyms = mapper.readValue(json, Map.class);
        } catch (IOException e) {
            synonyms = new HashMap<>();
        }
    }

   @GetMapping
   public List<Product> searchProducts(@RequestParam("query") String query) {
       String lowerQuery = query.toLowerCase();
       Set<String> relatedTypes = new HashSet<>();
       if (synonyms.containsKey(lowerQuery)) {
           relatedTypes.addAll(synonyms.get(lowerQuery));
       }
       List<Product> products = productRepository.findAll();
       List<Product> result = products.stream()
           .filter(p -> Boolean.TRUE.equals(p.getActive())
               && p.getTitle() != null
               && (p.getTitle().toLowerCase().contains(lowerQuery)
                   || relatedTypes.stream().anyMatch(type -> p.getTitle().toLowerCase().contains(type))))
           .collect(Collectors.toList());
       return result;
   }
}
