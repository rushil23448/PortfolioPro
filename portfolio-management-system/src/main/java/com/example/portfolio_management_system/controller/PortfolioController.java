package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.dto.DiversificationRecommendation;
import com.example.portfolio_management_system.dto.PortfolioAnalyticsResponse;
import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.repository.HolderRepository;
import com.example.portfolio_management_system.repository.HoldingRepository;
import com.example.portfolio_management_system.repository.StockRepository;
import com.example.portfolio_management_system.service.DiversificationService;
import com.example.portfolio_management_system.service.PortfolioAnalyticsService;
import com.example.portfolio_management_system.service.DumbMoneyService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final HolderRepository holderRepository;
    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;
    private final DiversificationService diversificationService;
    private final DumbMoneyService dumbMoneyService;

    private final PortfolioAnalyticsService analyticsService;

    public PortfolioController(HolderRepository holderRepository,
                               HoldingRepository holdingRepository,
                               StockRepository stockRepository, DiversificationService diversificationService, DumbMoneyService dumbMoneyService,
                               PortfolioAnalyticsService analyticsService) {

        this.holderRepository = holderRepository;
        this.holdingRepository = holdingRepository;
        this.stockRepository = stockRepository;
        this.diversificationService = diversificationService;
        this.dumbMoneyService = dumbMoneyService;
        this.analyticsService = analyticsService;
    }

    // ✅ API 1: Get all holders
   /* @GetMapping("/holders")
    public List<Holder> getAllHolders() {
        return holderRepository.findAll();
    }*/

    // ✅ API 2: Get portfolio of one holder
    @GetMapping("/portfolio/{holderId}")
    public List<Holding> getPortfolio(@PathVariable Long holderId) {
        return holdingRepository.findByHolderId(holderId);
    }

    // ✅ API 3: Portfolio Analytics
    @GetMapping("/{holderId}/analytics")
    public PortfolioAnalyticsResponse analytics(@PathVariable Long holderId) {
        return analyticsService.getAnalytics(holderId);
    }
    @GetMapping("/{holderId}/diversification")
    public List<DiversificationRecommendation> diversification(@PathVariable Long holderId) {
        return diversificationService.analyzeDiversification(holderId);
    }
    // ✅ API 5: Dumb Money Heatmap
    @GetMapping("/dumb-money/heatmap")
    public List<?> dumbMoneyHeatmap() {
        return dumbMoneyService.generateHeatmap();
    }

}
