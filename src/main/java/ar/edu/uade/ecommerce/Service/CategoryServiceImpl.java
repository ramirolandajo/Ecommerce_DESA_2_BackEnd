package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Category;
import ar.edu.uade.ecommerce.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> saveAllCategories(List<Category> categories) {
        List<Category> existingCategories = categoryRepository.findAll();
        List<String> incomingNames = categories.stream().map(Category::getName).collect(Collectors.toList());
        existingCategories.stream()
            .filter(c -> !incomingNames.contains(c.getName()))
            .forEach(categoryRepository::delete);
        for (Category incoming : categories) {
            Optional<Category> existing = categoryRepository.findByName(incoming.getName());
            if (existing.isPresent()) {
                Category c = existing.get();
                c.setName(incoming.getName());
                c.setActive(true);
                categoryRepository.save(c);
            } else {
                incoming.setActive(true);
                categoryRepository.save(incoming);
            }
        }
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void deleteAllCategories() {
        categoryRepository.deleteAll();
    }

    @Override
    public Category saveCategory(Category category) {
        if (category == null || category.getName() == null) {
            return null;
        }
        if (!category.isActive() && category.getId() != null) {
            category.setActive(false);
        } else {
            category.setActive(true);
        }
        return categoryRepository.save(category);
    }
}

