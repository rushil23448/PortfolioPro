package com.example.portfolio_management_system.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HoldingTest {

    @Test
    void builder_populatesRelations() {
        Stock stockr = Stock.builder()
                .symbol("TSLA")
                .name("Tesla")
                .sector("Automotive")
                .basePrice(600.0)
                .currentPrice(700.0)
                .volatility(0.5)
                .confidenceScore(50)
                .build();

        Holder holder = Holder.builder()
                .id(1L)
                .name("Alice")
                .build();

        Holding holding = Holding.builder()
                .id(10L)
                .quantity(2)
                .avgPrice(550.0)
                .stock(stock)
                .holder(holder)
                .build();

        assertEquals(2, holding.getQuantity());
        assertEquals(550.0, holding.getAvgPrice());
        assertNotNull(holding.getStock());
        assertEquals("TSLA", holding.getStock().getSymbol());
        assertNotNull(holding.getHolder());
        assertEquals("Alice", holding.getHolder().getName());
    }
}
