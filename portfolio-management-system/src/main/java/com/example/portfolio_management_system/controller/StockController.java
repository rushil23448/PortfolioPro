package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository;

    @GetMapping
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    @PostMapping("/add")
    public Stock addStock(@RequestBody Stock stock) {
        return stockRepository.save(stock);
    }
}
