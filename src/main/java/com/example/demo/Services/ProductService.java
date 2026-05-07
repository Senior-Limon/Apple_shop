package com.example.demo.Services;

import com.example.demo.Data.Product;
import com.example.demo.Repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Метод с поддержкой сортировки
    public List<Product> getProductsByCategoryIdSorted(Long categoryId, String sort) {
        if (sort == null || sort.equals("default")) {
            return productRepository.findByCategoryIdWithImages(categoryId);
        }
        switch (sort) {
            case "price_asc":
                return productRepository.findByCategoryIdOrderByPriceAsc(categoryId);
            case "price_desc":
                return productRepository.findByCategoryIdOrderByPriceDesc(categoryId);
            case "name_asc":
                return productRepository.findByCategoryIdOrderByNameAsc(categoryId);
            case "name_desc":
                return productRepository.findByCategoryIdOrderByNameDesc(categoryId);
            default:
                return productRepository.findByCategoryIdWithImages(categoryId);
        }
    }

    // Для списка товаров в категории (без сортировки – оставляем для обратной совместимости)
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryIdWithImages(categoryId);
    }

    // Для одного товара (детальная страница)
    public Product getProductById(Long id) {
        return productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден: " + id));
    }
}