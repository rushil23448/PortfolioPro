package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.dto.PortfolioAnalyticsResponse;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.HoldingRepository;
import com.example.portfolio_management_system.repository.HolderRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PortfolioAnalyticsService {

    private final HoldingRepository holdingRepository;
    private final HolderRepository holderRepository;

    public PortfolioAnalyticsService(HoldingRepository holdingRepository,
                                     HolderRepository holderRepository) {
        this.holdingRepository = holdingRepository;
        this.holderRepository = holderRepository;
    }

    public PortfolioAnalyticsResponse getAnalytics(Long holderId) {

        // ✅ Holder Name
        String holderName = holderRepository.findById(holderId)
                .orElseThrow()
                .getName();

        List<Holding> holdings = holdingRepository.findByHolderId(holderId);

        // If no holdings, return zeros/empty allocation
        if (holdings == null || holdings.isEmpty()) {
            return PortfolioAnalyticsResponse.builder()
                    .holderName(holderName)
                    .totalInvested(0.0)
                    .currentValue(0.0)
                    .profitLoss(0.0)
                    .diversificationScore(0)
                    .riskScore(0)
                    .sectorAllocation(new HashMap<>())
                    .build();
        }

        double totalInvested = 0;
        double currentValue = 0;

        // Sector Values Map
        Map<String, Double> sectorValues = new HashMap<>();

        // -----------------------------
        // ✅ Loop Holdings
        // -----------------------------
        for (Holding h : holdings) {

            Stock stock = h.getStock();

            double invested = h.getAvgPrice() * h.getQuantity();
            double current = stock.getCurrentPrice() * h.getQuantity();

            totalInvested += invested;
            currentValue += current;

            // Sector Allocation Value
            sectorValues.put(
                    stock.getSector(),
                    sectorValues.getOrDefault(stock.getSector(), 0.0) + current
            );
        }

        // -----------------------------
        // ✅ Profit/Loss
        // -----------------------------
        double profitLoss = currentValue - totalInvested;

        // -----------------------------
        // ✅ Sector Allocation %
        // -----------------------------
        Map<String, Double> sectorAllocation = new HashMap<>();

        for (String sector : sectorValues.keySet()) {

            double percent = (sectorValues.get(sector) / currentValue) * 100;

            sectorAllocation.put(sector, Math.round(percent * 100.0) / 100.0);
        }

        // -----------------------------
        // ✅ Diversification Score
        // -----------------------------
        int sectorCount = sectorAllocation.size();

        int diversificationScore = Math.min(100, sectorCount * 20);
        // Example:
        // 1 sector → 20
        // 5 sectors → 100

        // -----------------------------
        // ✅ Risk Score
        // -----------------------------
        double riskSum = 0;

        for (Holding h : holdings) {
            riskSum += h.getStock().getVolatility() * 100;
        }

        int riskScore = (int) Math.min(100, riskSum / holdings.size());

        // -----------------------------
        // ✅ Return DTO
        // -----------------------------
        return PortfolioAnalyticsResponse.builder()
                .holderName(holderName)
                .totalInvested(totalInvested)
                .currentValue(currentValue)
                .profitLoss(profitLoss)
                .diversificationScore(diversificationScore)
                .riskScore(riskScore)
                .sectorAllocation(sectorAllocation)
                .build();
    }
}
