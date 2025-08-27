package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> saveAllCategories(List<Category> categories);
    List<Category> getAllCategories();
    void deleteAllCategories();
    Category saveCategory(Category category);
}

