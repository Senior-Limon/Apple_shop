package com.example.demo.Controllers;

import com.example.demo.Data.Category;
import com.example.demo.Data.Product;
import com.example.demo.Repositories.CategoryRepository;
import com.example.demo.Repositories.ProductRepository;
import com.example.demo.Services.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;

    @GetMapping("/category/{slug}")
    public String showCategory(@PathVariable String slug,
                               @RequestParam(required = false) String sort,
                               Model model,
                               HttpServletRequest request) {
        try {
            Category category = categoryRepository.findBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Категория не найдена"));

            List<Product> products = productService.getProductsByCategoryIdSorted(category.getId(), sort);

            // Добавляем список всех категорий для навигации
            List<Category> allCategories = categoryRepository.findAllByOrderByNameAsc();

            model.addAttribute("categories", allCategories);
            model.addAttribute("categoryName", category.getName());
            model.addAttribute("products", products);
            model.addAttribute("currentSort", sort);
            model.addAttribute("redirectUrl", request.getRequestURI());

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Категория не найдена");
            return "error";
        }
        return "category";
    }
}