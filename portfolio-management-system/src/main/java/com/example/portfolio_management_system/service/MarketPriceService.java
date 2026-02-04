package com.example.portfolio_management_system.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class MarketPriceService {

    // Mock stock prices
    private static final Map<String, Double> mockPrices = new HashMap<>();

    static {
        mockPrices.put("TCS", 3850.0);
        mockPrices.put("INFY", 1650.0);
        mockPrices.put("HDFCBANK", 1500.0);
        mockPrices.put("RELIANCE", 2900.0);
        mockPrices.put("ITC", 450.0);
    }

    public Double getLivePrice(String symbol) {

        // If stock exists → return with small variation
        if (mockPrices.containsKey(symbol)) {
            double base = mockPrices.get(symbol);

            // Add random variation (+/- 2%)
            double variation = (new Random().nextDouble() * 0.04) - 0.02;

            return base + (base * variation);
        }

        // Default price if unknown stock
        return 100.0;
    }
}
