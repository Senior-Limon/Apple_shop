package com.example.demo.Controllers;

import com.example.demo.Data.Order;
import com.example.demo.Data.User;
import com.example.demo.Repositories.OrderRepository;
import com.example.demo.Repositories.ProductRepository;
import com.example.demo.Repositories.UserRepository;
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
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByPhone(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public String dashboard(Model model) {
        User currentUser = getCurrentUser();

        if (!"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/";
        }

        List<User> users = userRepository.findAll();
        long productCount = productRepository.count();
        long orderCount = orderRepository.count();
        long newOrdersCount = orderRepository.findByStatusOrderByCreatedAtDesc("NEW").size();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", users);
        model.addAttribute("productCount", productCount);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("newOrdersCount", newOrdersCount);
        model.addAttribute("orders", orderRepository.findAllSortedByStatus().stream().limit(5).toList());

        return "admin/dashboard";
    }

    @PostMapping("/user/delete")
    public String deleteUser(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();

        if (currentUser.getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "Нельзя удалить самого себя");
            return "redirect:/admin";
        }

        userRepository.deleteById(userId);
        redirectAttributes.addFlashAttribute("message", "Пользователь удалён");
        return "redirect:/admin";
    }
}