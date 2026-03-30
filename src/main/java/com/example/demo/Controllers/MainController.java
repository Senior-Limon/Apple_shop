package com.example.demo.Controllers;

import com.example.demo.Data.Category;
import com.example.demo.Repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/main")
    public String home(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "index";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

}