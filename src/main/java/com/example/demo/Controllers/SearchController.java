package com.example.demo.Controllers;

import com.example.demo.Data.Product;
import com.example.demo.Repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/api/search")
    public List<Map<String, Object>> searchProducts(@RequestParam(value = "q", required = false) String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (query == null || query.trim().length() < 2) {
            return results;
        }

        try {
            List<Product> products = productRepository.findByNameContainingIgnoreCase(query.trim());

            for (Product product : products) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", product.getId());
                item.put("name", product.getName());
                item.put("price", product.getPrice());

                // Получаем URL главного изображения
                String imageUrl = "/images/no-image.png";
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    imageUrl = product.getImages().get(0).getImageUrl();
                }
                item.put("imageUrl", imageUrl);

                results.add(item);
                if (results.size() >= 10) break;
            }
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
        }

        return results;
    }
}