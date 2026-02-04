package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.dto.PortfolioSummary;
import com.example.portfolio_management_system.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    // Portfolio Summary API
    @GetMapping("/summary/{holderId}")
    public PortfolioSummary getSummary(@PathVariable Long holderId) {
        return portfolioService.getPortfolioSummary(holderId);
    }
}
