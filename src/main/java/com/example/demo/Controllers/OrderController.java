package com.example.demo.Controllers;

import com.example.demo.Data.CartItem;
import com.example.demo.Data.Order;
import com.example.demo.Data.User;
import com.example.demo.Services.CartService;
import com.example.demo.Services.OrderService;
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
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserByPhone(auth.getName());
    }

    @GetMapping("/checkout")
    public String showCheckoutForm(Model model) {
        User user = getCurrentUser();
        List<CartItem> cartItems = cartService.getCartItems(user.getId());

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        double total = cartService.getCartTotal(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "checkout";
    }

    @PostMapping("/create")
    public String createOrder(@RequestParam String customerName,
                              @RequestParam String customerPhone,
                              @RequestParam String city,
                              @RequestParam String street,
                              @RequestParam String house,
                              @RequestParam(required = false) String apartment,
                              @RequestParam String paymentType,
                              @RequestParam(required = false) String comment,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();

            // Собираем полный адрес
            String deliveryAddress = city + ", " + street + ", " + house;
            if (apartment != null && !apartment.trim().isEmpty()) {
                deliveryAddress += ", кв. " + apartment;
            }

            // Получаем сумму корзины для расчёта доставки
            List<CartItem> cartItems = cartService.getCartItems(user.getId());
            double subtotal = cartItems.stream()
                    .mapToDouble(i -> i.getProduct().getPrice().doubleValue() * i.getQuantity())
                    .sum();
            double deliveryCost = (subtotal >= 1000) ? 0 : 30;
            double total = subtotal + deliveryCost;

            Order order = orderService.createOrderWithStockCheck(
                    user.getId(), customerName, customerPhone,
                    deliveryAddress, paymentType, comment
            );

            redirectAttributes.addFlashAttribute("message", "Заказ #" + order.getId() + " успешно оформлен!");
            return "redirect:/profile";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
}