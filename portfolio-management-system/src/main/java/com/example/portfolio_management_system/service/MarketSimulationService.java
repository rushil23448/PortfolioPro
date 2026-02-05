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

    /**
     * Runs every 5 seconds
     * Simulates market movement + updates confidence score
     */
    @Scheduled(fixedRate = 5000)
    public void updateStockPrices() {

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {

            // ----------------------------
            // 1. PRICE SIMULATION
            // ----------------------------
            double currentPrice = stock.getCurrentPrice();
            double volatility = stock.getVolatility();

            // random price change (-volatility% to +volatility%)
            double priceChangePercent =
                    (random.nextDouble() * 2 - 1) * volatility;

            double newPrice =
                    currentPrice * (1 + priceChangePercent);

            // prevent unrealistic crash
            newPrice = Math.max(newPrice, stock.getBasePrice() * 0.4);

            // ----------------------------
            // 2. CONFIDENCE UPDATE LOGIC
            // ----------------------------
            int confidence = stock.getConfidenceScore();

            if (priceChangePercent > 0) {
                // price went up
                if (priceChangePercent < 0.01) confidence += 1;
                else if (priceChangePercent < 0.03) confidence += 2;
                else confidence += 3;
            } else {
                // price went down
                if (priceChangePercent > -0.01) confidence -= 1;
                else if (priceChangePercent > -0.03) confidence -= 2;
                else confidence -= 4;
            }

            // volatility penalty
            if (volatility > 0.30) confidence -= 1;

            // clamp confidence between 0 and 100
            confidence = Math.max(0, Math.min(100, confidence));

            // ----------------------------
            // 3. SAVE UPDATES
            // ----------------------------
            stock.setCurrentPrice(newPrice);
            stock.setConfidenceScore(confidence);

            stockRepository.save(stock);
        }

        System.out.println("📈 Market prices & confidence updated");
    }
}