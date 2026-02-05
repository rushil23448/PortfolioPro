package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.model.DumbMoneySignal;
import com.example.portfolio_management_system.model.Stock;
import org.springframework.stereotype.Service;

@Service
public class DumbMoneyService {

    /**
     * Classifies stock behavior using:
     * - volatility
     * - confidence score
     * - price deviation from base price
     */
    public DumbMoneySignal evaluate(Stock stock) {

        double priceDeviation =
                (stock.getCurrentPrice() - stock.getBasePrice())
                        / stock.getBasePrice();

        // 🚨 Dumb Money Conditions
        if (stock.getVolatility() > 0.30 &&
                stock.getConfidenceScore() < 60 &&
                priceDeviation > 0.20) {

            return DumbMoneySignal.DUMB_MONEY;
        }

        // 💰 Smart Money Conditions
        if (stock.getConfidenceScore() > 80 &&
                stock.getVolatility() < 0.22 &&
                priceDeviation > 0) {

            return DumbMoneySignal.SMART_MONEY;
        }

        return DumbMoneySignal.NEUTRAL;
    }
}