package com.example.demo.Controllers;

import com.example.demo.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PhoneCheckController {

    @Autowired
    private UserService userService;

    @GetMapping("/api/check-phone")
    public Map<String, Object> checkPhone(@RequestParam String phone) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userService.existsByPhone(phone);
        response.put("exists", exists);
        response.put("message", exists ? "Phone number already registered" : "Phone number available");
        return response;
    }
}