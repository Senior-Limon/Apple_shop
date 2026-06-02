package com.example.demo.Controllers;

import com.example.demo.Data.Order;
import com.example.demo.Data.User;
import com.example.demo.Services.OrderService;
import com.example.demo.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserByPhone(auth.getName());
    }

    @GetMapping
    public String profilePage(Model model) {
        User user = getCurrentUser();
        List<Order> orders = orderService.getUserOrders(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        return "profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser();

        if (!userService.login(user.getPhone(), currentPassword)) {
            redirectAttributes.addFlashAttribute("error", "Текущий пароль неверен");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Новый пароль и подтверждение не совпадают");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Пароль должен быть минимум 6 символов");
            return "redirect:/profile";
        }

        userService.changePassword(user.getId(), newPassword);
        redirectAttributes.addFlashAttribute("message", "Пароль успешно изменён");

        return "redirect:/profile";
    }
}