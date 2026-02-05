package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.dto.StockRecommendation;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.HoldingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    private final HoldingRepository holdingRepository;

    public RecommendationService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    public List<StockRecommendation> getRecommendations(Long holderId) {

        List<Holding> holdings = holdingRepository.findByHolderId(holderId);
        List<StockRecommendation> recommendations = new ArrayList<>();

        for (Holding holding : holdings) {

            Stock stock = holding.getStock();

            double buyPrice = holding.getAvgPrice();
            double currentPrice = stock.getCurrentPrice();

            double pnlPercent =
                    ((currentPrice - buyPrice) / buyPrice) * 100;

            String decision;
            String reason;

            int confidence = stock.getConfidenceScore();
            double volatility = stock.getVolatility();

            // ----------------------------
            // AI RULE ENGINE
            // ----------------------------

            if (confidence >= 75 && pnlPercent < 5 && volatility < 0.30) {
                decision = "BUY";
                reason = "High confidence, low volatility, growth potential";
            }
            else if (confidence >= 60 && pnlPercent >= 5) {
                decision = "HOLD";
                reason = "Good performance, stable confidence";
            }
            else if (confidence < 50 || pnlPercent < -8) {
                decision = "SELL";
                reason = "Low confidence or rising downside risk";
            }
            else {
                decision = "HOLD";
                reason = "Neutral signals, wait for clarity";
            }

            recommendations.add(
                    StockRecommendation.builder()
                            .stockSymbol(stock.getSymbol())
                            .stockName(stock.getName())
                            .buyPrice(buyPrice)
                            .currentPrice(currentPrice)
                            .profitLossPercent(pnlPercent)
                            .confidenceScore(confidence)
                            .volatility(volatility)
                            .recommendation(decision)
                            .reason(reason)
                            .build()
            );
        }

        return recommendations;
    }
}