package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.model.DumbMoneySignal;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DumbMoneyService {

    private final StockRepository stockRepository;

    public DumbMoneyService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // 🔥 FIXED: returns ENUM
    public DumbMoneySignal evaluate(Stock stock) {

        double volatility = stock.getVolatility();
        int confidence = stock.getConfidenceScore();

        if (volatility > 0.35 && confidence < 70) {
            return DumbMoneySignal.DUMB_MONEY;
        }

        if (volatility < 0.25 && confidence > 85) {
            return DumbMoneySignal.SMART_MONEY;
        }

        return DumbMoneySignal.NEUTRAL;
    }

    // 🔥 Heatmap
    public List<DumbMoneySignal> generateHeatmap() {

        List<DumbMoneySignal> heatmap = new ArrayList<>();

        for (Stock stock : stockRepository.findAll()) {
            heatmap.add(evaluate(stock));
        }

        return heatmap;
    }
}
