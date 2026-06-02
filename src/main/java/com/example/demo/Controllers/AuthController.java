package com.example.demo.Controllers;

import com.example.demo.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("fullName")) {
            model.addAttribute("fullName", "");
        }
        if (!model.containsAttribute("countryCode")) {
            model.addAttribute("countryCode", "BY");
        }
        if (!model.containsAttribute("phoneNumber")) {
            model.addAttribute("phoneNumber", "");
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String fullName,
            @RequestParam String password,
            @RequestParam String countryCode,
            @RequestParam String phoneNumber,
            RedirectAttributes redirectAttributes) {

        try {
            userService.register(fullName, password, countryCode, phoneNumber);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("fullName", fullName);
            redirectAttributes.addFlashAttribute("countryCode", countryCode);
            redirectAttributes.addFlashAttribute("phoneNumber", phoneNumber);
            return "redirect:/register";
        }
    }
}