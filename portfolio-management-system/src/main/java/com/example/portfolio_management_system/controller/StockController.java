package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.StockRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class StockController {

    private final StockRepository stockRepository;

    public StockController(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // ✅ API: Get all stocks with live prices
    @GetMapping
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }
}
