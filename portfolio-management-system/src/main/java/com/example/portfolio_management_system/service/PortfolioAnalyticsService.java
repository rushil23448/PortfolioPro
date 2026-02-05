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

        // Holder Name
        String holderName = holderRepository.findById(holderId)
                .orElseThrow()
                .getName();

        List<Holding> holdings = holdingRepository.findByHolderId(holderId);

        if (holdings.isEmpty()) {
            return PortfolioAnalyticsResponse.builder()
                    .holderName(holderName)
                    .totalInvested(0.0)
                    .currentValue(0.0)
                    .profitLoss(0.0)
                    .totalHoldings(0)
                    .uniqueStocks(0)
                    .averageReturn(0.0)
                    .bestPerformer("-")
                    .diversificationScore(0)
                    .riskScore(0)
                    .sectorAllocation(new HashMap<>())
                    .build();
        }

        double totalInvested = 0;
        double currentValue = 0;

        // Sector Values Map
        Map<String, Double> sectorValues = new HashMap<>();
        
        // Track unique stocks and best performer
        Set<String> uniqueStocks = new HashSet<>();
        String bestPerformer = "-";
        double bestReturn = Double.MIN_VALUE;

        // Loop Holdings
        for (Holding h : holdings) {

            Stock stock = h.getStock();
            uniqueStocks.add(stock.getSymbol());

            double invested = h.getAvgPrice() * h.getQuantity();
            double current = stock.getCurrentPrice() * h.getQuantity();
            double returnPercent = ((stock.getCurrentPrice() - h.getAvgPrice()) / h.getAvgPrice()) * 100;

            totalInvested += invested;
            currentValue += current;

            // Track best performer
            if (returnPercent > bestReturn) {
                bestReturn = returnPercent;
                bestPerformer = stock.getSymbol() + " (" + String.format("%.2f", returnPercent) + "%)";
            }

            // Sector Allocation Value
            sectorValues.put(
                    stock.getSector(),
                    sectorValues.getOrDefault(stock.getSector(), 0.0) + current
            );
        }

        // Profit/Loss
        double profitLoss = currentValue - totalInvested;

        // Average Return
        double avgReturn = totalInvested > 0 ? ((currentValue - totalInvested) / totalInvested) * 100 : 0;

        // Sector Allocation %
        Map<String, Double> sectorAllocation = new HashMap<>();

        for (String sector : sectorValues.keySet()) {
            double percent = (sectorValues.get(sector) / currentValue) * 100;
            sectorAllocation.put(sector, Math.round(percent * 100.0) / 100.0);
        }

        // Diversification Score
        int sectorCount = sectorAllocation.size();
        int diversificationScore = Math.min(100, sectorCount * 20);

        // Risk Score
        double riskSum = 0;
        for (Holding h : holdings) {
            riskSum += h.getStock().getVolatility() * 100;
        }
        int riskScore = (int) Math.min(100, riskSum / holdings.size());

        // Return DTO
        return PortfolioAnalyticsResponse.builder()
                .holderName(holderName)
                .totalInvested(totalInvested)
                .currentValue(currentValue)
                .profitLoss(profitLoss)
                .diversificationScore(diversificationScore)
                .riskScore(riskScore)
                .totalHoldings(holdings.size())
                .uniqueStocks(uniqueStocks.size())
                .averageReturn(Math.round(avgReturn * 100.0) / 100.0)
                .bestPerformer(bestPerformer)
                .sectorAllocation(sectorAllocation)
                .build();
    }
}

