package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.service.HoldingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HoldingController {

    private final HoldingService holdingService;

    // ✅ Helper DTO matches the JSON sent from app.js
    @Data
    public static class AddHoldingRequest {
        private Long holderId;
        private String stockSymbol; // ✅ String (e.g., "AAPL")
        private Integer quantity;
        private Double price;
    }

    @PostMapping("/add")
    public Holding addHolding(@RequestBody AddHoldingRequest request) {
        return holdingService.addHolding(
                request.getHolderId(),
                request.getStockSymbol(),
                request.getQuantity(),
                request.getPrice()
        );
    }

    @GetMapping("/{holderId}")
    public List<Holding> getHoldings(@PathVariable Long holderId) {
        return holdingService.getHoldingsByHolder(holderId);
    }
}