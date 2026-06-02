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
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("Please login to view cart");
        }
        return userService.getUserByPhone(auth.getName());
    }

    @GetMapping
    public String showCart(Model model) {
        try {
            User user = getCurrentUser();
            List<CartItem> items = cartService.getCartItems(user.getId());
            double total = items.stream()
                    .mapToDouble(i -> i.getProduct().getPrice().doubleValue() * i.getQuantity())
                    .sum();
            model.addAttribute("cartItems", items);
            model.addAttribute("total", total);
            model.addAttribute("itemsCount", items.size());
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
        }
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            cartService.addToCart(user.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long cartItemId,
                                 @RequestParam int quantity,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            if (quantity < 1) quantity = 1;
            cartService.updateQuantity(user.getId(), cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Quantity updated");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam Long cartItemId,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            cartService.removeItem(user.getId(), cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }
}