package ar.edu.uade.ecommerce.Service;

import ar.edu.uade.ecommerce.Entity.Brand;

import java.util.Collection;
import java.util.List;

public interface BrandService {
    List<Brand> saveAllBrands(List<Brand> brands);
    List<Brand> getAllBrands();
    void deleteAllBrands();
    Brand saveBrand(Brand brand);

    Collection<Object> getAllActiveBrands();
}
