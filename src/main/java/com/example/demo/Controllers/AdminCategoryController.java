package com.example.demo.Controllers;

import com.example.demo.Data.Category;
import com.example.demo.Data.User;
import com.example.demo.Repositories.CategoryRepository;
import com.example.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByPhone(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public String listCategories(Model model) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        model.addAttribute("categories", categories);
        return "admin/categories";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("category", new Category());
        return "admin/category-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/";
        }
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("category", category);
        return "admin/category-form";
    }

    @PostMapping("/save")
    public String saveCategory(@RequestParam(required = false) Long id,
                               @RequestParam String name,
                               @RequestParam String slug,
                               RedirectAttributes redirectAttributes) {
        try {
            String processedSlug = slug.toLowerCase().trim().replaceAll("\\s+", "-");

            if (id != null && id > 0) {
                Category category = categoryRepository.findById(id).orElse(null);
                if (category == null) {
                    redirectAttributes.addFlashAttribute("error", "Категория не найдена");
                    return "redirect:/admin/categories";
                }
                if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
                    redirectAttributes.addFlashAttribute("error", "Категория с таким именем уже существует");
                    return "redirect:/admin/categories/edit/" + id;
                }
                if (!category.getSlug().equals(processedSlug) && categoryRepository.existsBySlug(processedSlug)) {
                    redirectAttributes.addFlashAttribute("error", "Категория с таким slug уже существует");
                    return "redirect:/admin/categories/edit/" + id;
                }
                category.setName(name);
                category.setSlug(processedSlug);
                categoryRepository.save(category);
                redirectAttributes.addFlashAttribute("message", "Категория обновлена");
            } else {
                if (categoryRepository.existsByName(name)) {
                    redirectAttributes.addFlashAttribute("error", "Категория с таким именем уже существует");
                    return "redirect:/admin/categories/add";
                }
                if (categoryRepository.existsBySlug(processedSlug)) {
                    redirectAttributes.addFlashAttribute("error", "Категория с таким slug уже существует");
                    return "redirect:/admin/categories/add";
                }
                Category category = new Category();
                category.setName(name);
                category.setSlug(processedSlug);
                long maxOrder = categoryRepository.count();
                category.setSortOrder((int) maxOrder);
                categoryRepository.save(category);
                redirectAttributes.addFlashAttribute("message", "Категория добавлена");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryRepository.findById(id).orElse(null);
            if (category == null) {
                redirectAttributes.addFlashAttribute("error", "Категория не найдена");
                return "redirect:/admin/categories";
            }
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Категория \"" + category.getName() + "\" удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка удаления. Возможно, в категории есть товары.");
        }
        return "redirect:/admin/categories";
    }
}