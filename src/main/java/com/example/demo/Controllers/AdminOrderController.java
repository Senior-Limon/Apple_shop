package com.example.demo.Controllers;

import com.example.demo.Data.Order;
import com.example.demo.Data.User;
import com.example.demo.Repositories.OrderRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportService reportService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByPhone(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public String listOrders(@RequestParam(required = false) String status, Model model) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/";
        }

        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
            model.addAttribute("currentStatus", status);
        } else {
            orders = orderRepository.findAllSortedByStatus();
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", List.of("NEW", "SHIPPED", "DELIVERED", "CANCELLED"));
        return "admin/orders";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long orderId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
        redirectAttributes.addFlashAttribute("message", "Статус заказа обновлён");
        return "redirect:/admin/orders";
    }

    @GetMapping("/report/word")
    public ResponseEntity<InputStreamResource> exportToWord(@RequestParam(required = false) String status) throws IOException {
        List<Order> orders;

        // Если передан статус - экспортируем только заказы с этим статусом
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            orders = orderRepository.findAllSortedByStatus();
        }

        ByteArrayInputStream report = reportService.generateOrdersReport(orders);

        // Формируем имя файла с учётом фильтра
        String filename = "orders_report";
        if (status != null) {
            String statusName = switch (status) {
                case "NEW" -> "new";
                case "SHIPPED" -> "shipped";
                case "DELIVERED" -> "delivered";
                case "CANCELLED" -> "cancelled";
                default -> status.toLowerCase();
            };
            filename = "orders_report_" + statusName;
        }
        filename += ".docx";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(report));
    }
}