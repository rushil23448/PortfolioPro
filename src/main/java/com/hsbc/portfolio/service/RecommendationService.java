package com.hsbc.portfolio.service;

import com.hsbc.portfolio.dto.RecommendationDTO;
import com.hsbc.portfolio.dto.StockDTO;
import com.hsbc.portfolio.entity.Stock;
import com.hsbc.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Recommendation engine: BUY / SELL / HOLD based on price momentum,
 * PE, and dumb money heat (avoid overheated).
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final StockRepository stockRepository;
    private final DumbMoneyService dumbMoneyService;

    public List<RecommendationDTO> getRecommendations(int limit) {
        List<Stock> stocks = stockRepository.findAll();
        Map<Long, Double> heatByStock = dumbMoneyService.getHeatScoresByStock();

        List<RecommendationDTO> recommendations = stocks.stream()
                .map(s -> buildRecommendation(s, heatByStock.getOrDefault(s.getId(), 0.0)))
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());

        return recommendations;
    }

    public List<RecommendationDTO> getBuyRecommendations(int limit) {
        return getRecommendations(limit * 2).stream()
                .filter(r -> "BUY".equals(r.getAction()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<RecommendationDTO> getSellRecommendations(int limit) {
        return getRecommendations(limit * 2).stream()
                .filter(r -> "SELL".equals(r.getAction()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private RecommendationDTO buildRecommendation(Stock s, double heatScore) {
        double change = s.getChangePercent() != null ? s.getChangePercent() : 0;
        double pe = s.getPeRatio() != null ? s.getPeRatio() : 20;
        double score = 50.0;
        String action = "HOLD";
        String reason = "Neutral momentum and valuation.";

        // Momentum: strong up = avoid chasing (FOMO), strong down = potential buy
        if (change > 15 && heatScore > 60) {
            action = "SELL";
            score = 75;
            reason = "Stock is overheated with high retail/FOMO flow. Consider booking profits.";
        } else if (change > 20) {
            action = "HOLD";
            score = 55;
            reason = "Strong run-up; wait for pullback before adding.";
        } else if (change < -10 && heatScore < 40) {
            action = "BUY";
            score = 70;
            reason = "Price correction with low emotional flow. Potential value.";
        } else if (pe < 15 && change < 5) {
            action = "BUY";
            score = 65;
            reason = "Reasonable valuation and stable price.";
        } else if (pe > 40 && change > 10) {
            action = "SELL";
            score = 68;
            reason = "High PE and extended price. Risk of correction.";
        } else if (change > 5 && change < 15) {
            action = "HOLD";
            score = 58;
            reason = "Moderate momentum. Hold existing position.";
        }

        BigDecimal target = s.getCurrentPrice() != null
                ? s.getCurrentPrice().multiply(BigDecimal.valueOf(1 + (action.equals("BUY") ? 0.1 : action.equals("SELL") ? -0.05 : 0)))
                : BigDecimal.ZERO;

        return RecommendationDTO.builder()
                .stock(StockService.toDTOStatic(s))
                .action(action)
                .reason(reason)
                .score(score)
                .targetPrice(target.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
