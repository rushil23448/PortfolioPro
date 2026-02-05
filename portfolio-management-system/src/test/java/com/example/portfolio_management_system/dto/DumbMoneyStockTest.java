package com.example.portfolio_management_system.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DumbMoneyStockTest {

    @Test
    void builder_setsAllFields() {
        DumbMoneyStock s = DumbMoneyStock.builder()
                .symbol("AAPL")
                .sector("Technology")
                .volatility(0.42)
                .confidenceScore(60)
                .label("DUMB MONEY")
                .build();

        assertEquals("AAPL", s.getSymbol());
        assertEquals("Technology", s.getSector());
        assertEquals(0.42, s.getVolatility());
        assertEquals(60, s.getConfidenceScore());
        assertEquals("DUMB MONEY", s.getLabel());
    }

}
