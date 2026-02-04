package com.example.portfolio_management_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Portfolio Management Backend Running Successfully 🚀";
    }

    @GetMapping("/api/test")
    public String test() {
        return "API Working Fine ✅";
    }
}
