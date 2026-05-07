package com.example.demo.Controllers;

import com.example.demo.Data.CartItem;
import com.example.demo.Data.User;
import com.example.demo.Services.CartService;
import com.example.demo.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не авторизован");
        }
        return userService.getUserByLogin(auth.getName());
    }

    @GetMapping
    public String viewCart(Model model) {
        User user = getCurrentUser();
        List<CartItem> items = cartService.getCartItems(user.getId());
        double total = items.stream()
                .mapToDouble(i -> i.getProduct().getPrice().doubleValue() * i.getQuantity())
                .sum();
        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        model.addAttribute("itemsCount", items.size());
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(getCurrentUser().getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("message", "Товар добавлен в корзину");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long cartItemId,
                                 @RequestParam int quantity,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (quantity < 1) quantity = 1;
            cartService.updateQuantity(getCurrentUser().getId(), cartItemId, quantity);
            redirectAttributes.addFlashAttribute("message", "Количество обновлено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam Long cartItemId,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItem(getCurrentUser().getId(), cartItemId);
            redirectAttributes.addFlashAttribute("message", "Товар удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }
}