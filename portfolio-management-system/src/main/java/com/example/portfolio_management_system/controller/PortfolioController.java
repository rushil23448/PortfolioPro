package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.dto.PortfolioAnalyticsResponse;
import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.repository.HolderRepository;
import com.example.portfolio_management_system.service.PortfolioAnalyticsService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final HolderRepository holderRepository;
    private final PortfolioAnalyticsService analyticsService;

    public PortfolioController(HolderRepository holderRepository,
                               PortfolioAnalyticsService analyticsService) {

        this.holderRepository = holderRepository;
        this.analyticsService = analyticsService;
    }

    // API 1: Get all holders
    @GetMapping("/holders")
    public List<Holder> getAllHolders() {
        return holderRepository.findAll();
    }

    // API 2: Add new holder
    @PostMapping("/holders/add")
    public Holder addHolder(@RequestBody Holder holder) {
        return holderRepository.save(holder);
    }

    // API 3: Portfolio Summary
    @GetMapping("/portfolio/summary/{holderId}")
    public PortfolioAnalyticsResponse getSummary(@PathVariable Long holderId) {
        return analyticsService.getAnalytics(holderId);
    }
}

