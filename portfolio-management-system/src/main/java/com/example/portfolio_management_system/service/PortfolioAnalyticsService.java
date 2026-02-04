package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.dto.PortfolioAnalyticsResponse;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.HoldingRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PortfolioAnalyticsService {

    private final HoldingRepository holdingRepository;

    public PortfolioAnalyticsService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    public PortfolioAnalyticsResponse getAnalytics(Long holderId) {

        List<Holding> holdings = holdingRepository.findByHolderId(holderId);

        double invested = 0;
        double current = 0;

        Map<String, Double> sectorMap = new HashMap<>();

        for (Holding h : holdings) {

            Stock stock = h.getStock();

            double buyValue = h.getAvgPrice() * h.getQuantity();
            double currentValue = stock.getBasePrice() * h.getQuantity();

            invested += buyValue;
            current += currentValue;

            sectorMap.put(
                    stock.getSector(),
                    sectorMap.getOrDefault(stock.getSector(), 0.0) + currentValue
            );
        }

        double profitLoss = current - invested;

        // Convert sector values into percentages
        Map<String, Double> allocationPercent = new HashMap<>();
        for (String sector : sectorMap.keySet()) {
            allocationPercent.put(sector,
                    (sectorMap.get(sector) / current) * 100
            );
        }

        // Diversification Score
        int diversificationScore = 100 - (allocationPercent.size() * 10);

        // Risk Score (simple avg volatility)
        int riskScore = holdings.stream()
                .map(h -> h.getStock().getVolatility())
                .mapToInt(v -> (int) (v * 100))
                .sum() / holdings.size();

        return PortfolioAnalyticsResponse.builder()
                .holderName(holdings.get(0).getHolder().getName())
                .totalInvested(invested)
                .currentValue(current)
                .profitLoss(profitLoss)
                .diversificationScore(diversificationScore)
                .riskScore(riskScore)
                .sectorAllocation(allocationPercent)
                .build();
    }
}
