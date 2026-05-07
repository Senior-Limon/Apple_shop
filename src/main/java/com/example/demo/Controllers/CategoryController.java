package com.example.demo.Controllers;

import com.example.demo.Data.Category;
import com.example.demo.Data.Product;
import com.example.demo.Services.CategoryService;
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
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping("/category/{slug}")
    public String showCategory(@PathVariable String slug,
                               @RequestParam(required = false) String sort,
                               Model model,
                               HttpServletRequest request) {
        Category category = categoryService.findBySlug(slug);
        List<Product> products = productService.getProductsByCategoryIdSorted(category.getId(), sort);

        model.addAttribute("categoryName", category.getName());
        model.addAttribute("products", products);
        model.addAttribute("redirectUrl", request.getRequestURI()); // если нужно для возврата на эту же страницу
        model.addAttribute("currentSort", sort); // можно передать в шаблон для подсветки выбранного значения
        return "category";
    }
}