package com.example.demo.Controllers;

import com.example.demo.Data.Category;
import com.example.demo.Data.Product;
import com.example.demo.Repositories.CategoryRepository;
import com.example.demo.Services.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/product/{id}")
    public String showProduct(@PathVariable Long id, Model model, HttpServletRequest request) {
        try {
            Product product = productService.getProductById(id);

            // Добавляем список всех категорий для навигации
            List<Category> allCategories = categoryRepository.findAllByOrderByNameAsc();

            model.addAttribute("categories", allCategories);
            model.addAttribute("product", product);
            model.addAttribute("redirectUrl", request.getRequestURI());

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Товар не найден");
            return "error";
        }
        return "product";
    }
}