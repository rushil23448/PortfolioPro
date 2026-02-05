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
    private final DumbMoneyService dumbMoneyService;
    private final Random random = new Random();

    public MarketSimulationService(StockRepository stockRepository,
                                   DumbMoneyService dumbMoneyService) {
        this.stockRepository = stockRepository;
        this.dumbMoneyService = dumbMoneyService;
    }

    @Scheduled(fixedRate = 5000)
    public void updateStockPrices() {

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {

            double currentPrice =
                    stock.getCurrentPrice() != null
                            ? stock.getCurrentPrice()
                            : stock.getBasePrice();

            double volatility = stock.getVolatility();

            // 📈 Price movement
            double priceChangePercent =
                    (random.nextDouble() * 2 - 1) * volatility;

            double newPrice =
                    currentPrice * (1 + priceChangePercent);

            newPrice = Math.max(newPrice, stock.getBasePrice() * 0.4);

            // round price
            newPrice = Math.round(newPrice * 100.0) / 100.0;

            // 🧠 Confidence logic
            int confidence = stock.getConfidenceScore();

            if (priceChangePercent > 0) {
                if (priceChangePercent < 0.01) confidence += 1;
                else if (priceChangePercent < 0.03) confidence += 2;
                else confidence += 3;
            } else {
                if (priceChangePercent > -0.01) confidence -= 1;
                else if (priceChangePercent > -0.03) confidence -= 2;
                else confidence -= 4;
            }

            if (volatility > 0.30) confidence -= 1;

            confidence = Math.max(0, Math.min(100, confidence));

            // 🔥 Dumb Money Evaluation
            stock.setCurrentPrice(newPrice);
            stock.setConfidenceScore(confidence);
            stock.setDumbMoneySignal(
                    dumbMoneyService.evaluate(stock)
            );

            stockRepository.save(stock);
        }

        System.out.println("📊 Market + Confidence + Dumb Money updated");
    }
}
