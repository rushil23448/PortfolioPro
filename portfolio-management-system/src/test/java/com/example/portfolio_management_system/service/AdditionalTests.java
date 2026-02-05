package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.model.DumbMoneySignal;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.HoldingRepository;
import com.example.portfolio_management_system.repository.HolderRepository;
import com.example.portfolio_management_system.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdditionalTests {

    @Test
    void stock_builder_setsFields() {
        Stock s = Stock.builder()
                .symbol("XYZ")
                .name("XYZ Corp")
                .sector("Utilities")
                .volatility(0.15)
                .confidenceScore(75)
                .currentPrice(42.5)
                .build();

        assertEquals("XYZ", s.getSymbol());
        assertEquals("XYZ Corp", s.getName());
        assertEquals("Utilities", s.getSector());
        assertEquals(0.15, s.getVolatility());
        assertEquals(75, s.getConfidenceScore());
        assertEquals(42.5, s.getCurrentPrice());
    }

    @Mock
    StockRepository stockRepository;

    @InjectMocks
    DumbMoneyService dumbMoneyService;

    @Test
    void evaluate_boundaryValues_returnsNeutral() {
        // volatility == 0.35 (not > 0.35) and confidence == 70 (not < 70) => NEUTRAL
        Stock s = Stock.builder()
                .symbol("BND")
                .volatility(0.35)
                .confidenceScore(70)
                .build();

        DumbMoneySignal sig = dumbMoneyService.evaluate(s);
        assertEquals(DumbMoneySignal.NEUTRAL, sig);
    }

    @Test
    void generateHeatmap_emptyRepository_returnsEmptyList() {
        when(stockRepository.findAll()).thenReturn(List.of());

        List<DumbMoneySignal> heatmap = dumbMoneyService.generateHeatmap();
        assertNotNull(heatmap);
        assertTrue(heatmap.isEmpty());
    }

    @Mock
    HoldingRepository holdingRepository;

    @Mock
    HolderRepository holderRepository;

    @InjectMocks
    PortfolioAnalyticsService analyticsService;

    @Test
    void analytics_singleHolding_computesExpectedValues() {
        Long holderId = 99L;
        Holder holder = Holder.builder().id(holderId).name("Solo").build();

        Stock stock = Stock.builder()
                .symbol("ONE")
                .sector("Health")
                .currentPrice(50.0)
                .volatility(0.12)
                .build();

        Holding h = Holding.builder()
                .id(123L)
                .quantity(3)
                .avgPrice(40.0)
                .stock(stock)
                .holder(holder)
                .build();

        when(holderRepository.findById(holderId)).thenReturn(Optional.of(holder));
        when(holdingRepository.findByHolderId(holderId)).thenReturn(List.of(h));

        var resp = analyticsService.getAnalytics(holderId);

        // Invested = 3*40 = 120
        assertEquals(120.0, resp.getTotalInvested());

        // Current = 3*50 = 150
        assertEquals(150.0, resp.getCurrentValue());

        // Profit = 30
        assertEquals(30.0, resp.getProfitLoss());

        // Diversification: 1 sector -> 20
        assertEquals(20, resp.getDiversificationScore());

        // Risk score = volatility*100 cast to int -> 12
        assertEquals(12, resp.getRiskScore());

        // Sector allocation: single sector 100%
        assertEquals(1, resp.getSectorAllocation().size());
        assertEquals(100.0, resp.getSectorAllocation().get("Health"));
    }

    @Test
    void analytics_emptyHoldings_returnsZeros() {
        Long holderId = 100L;
        Holder holder = Holder.builder().id(holderId).name("Empty").build();

        when(holderRepository.findById(holderId)).thenReturn(Optional.of(holder));
        when(holdingRepository.findByHolderId(holderId)).thenReturn(List.of());

        var resp = analyticsService.getAnalytics(holderId);

        assertEquals(0.0, resp.getTotalInvested());
        assertEquals(0.0, resp.getCurrentValue());
        assertEquals(0.0, resp.getProfitLoss());
        assertEquals(0, resp.getDiversificationScore());
        assertEquals(0, resp.getRiskScore());
        assertNotNull(resp.getSectorAllocation());
        assertTrue(resp.getSectorAllocation().isEmpty());
    }

    @Test
    void getAnalytics_missingHolder_throwsException() {
        Long holderId = 101L;

        when(holderRepository.findById(holderId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> analyticsService.getAnalytics(holderId));
    }

}
