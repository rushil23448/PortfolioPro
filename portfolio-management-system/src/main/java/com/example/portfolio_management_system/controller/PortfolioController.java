package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.HolderRepository;
import com.example.portfolio_management_system.repository.HoldingRepository;
import com.example.portfolio_management_system.repository.StockRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final HolderRepository holderRepository;
    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;

    public PortfolioController(HolderRepository holderRepository,
                               HoldingRepository holdingRepository,
                               StockRepository stockRepository) {
        this.holderRepository = holderRepository;
        this.holdingRepository = holdingRepository;
        this.stockRepository = stockRepository;
    }

    // ✅ API 1: Get all holders
    @GetMapping("/holders")
    public List<Holder> getAllHolders() {
        return holderRepository.findAll();
    }

    // ✅ API 2: Get portfolio of one holder
    @GetMapping("/portfolio/{holderId}")
    public List<Holding> getPortfolio(@PathVariable Long holderId) {
        return holdingRepository.findByHolderId(holderId);
    }



}
