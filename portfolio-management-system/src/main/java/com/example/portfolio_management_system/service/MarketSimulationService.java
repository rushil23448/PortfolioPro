package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.StockRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class MarketSimulationService {

    private final StockRepository stockRepository;
    private final Random random = new Random();

    public MarketSimulationService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // ✅ Market Updates Every 5 Seconds
    @Scheduled(fixedRate = 5000)
    public void updateStockPrices() {

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {

            double base = stock.getBasePrice();
            double volatility = stock.getVolatility();
            int confidence = stock.getConfidenceScore();

            // Confidence stabilizes price movement
            double marketFactor = (100 - confidence) / 100.0;

            // Random movement between -0.5 and +0.5
            double randomMove =
                    (random.nextDouble() - 0.5)
                            * base
                            * volatility
                            * marketFactor;

            double newPrice = stock.getCurrentPrice() + randomMove;

            // Prevent negative price
            stock.setCurrentPrice(Math.max(newPrice, 1));
        }

        stockRepository.saveAll(stocks);

        System.out.println("📈 Market prices updated successfully!");
    }
}
