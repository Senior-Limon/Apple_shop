package com.example.demo.Controllers;

import com.example.demo.Data.Product;
import com.example.demo.Services.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/product/{id}")
    public String showProduct(@PathVariable Long id, Model model, HttpServletRequest request) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("redirectUrl", request.getRequestURI()); // для возврата на эту же страницу после добавления в корзину
        return "product";
    }
}