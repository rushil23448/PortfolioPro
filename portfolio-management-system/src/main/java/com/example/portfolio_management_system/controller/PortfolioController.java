package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.repository.HoldingRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin("*")
public class PortfolioController {

    private final HoldingRepository holdingRepository;

    public PortfolioController(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    // ✅ Get portfolio of holder by ID
    @GetMapping("/{holderId}")
    public List<Holding> getPortfolio(@PathVariable Long holderId) {
        return holdingRepository.findByHolderId(holderId);
    }
}
