package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Product;

public interface ProductService {
    Product updateProductStock(Integer productId, int newStock);

    Product findById(Long productId);
}

