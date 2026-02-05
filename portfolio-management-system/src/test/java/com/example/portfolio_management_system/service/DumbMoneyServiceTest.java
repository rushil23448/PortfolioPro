package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.model.DumbMoneySignal;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DumbMoneyServiceTest {

    @Mock
    StockRepository stockRepository;

    @InjectMocks
    DumbMoneyService dumbMoneyService;

    @Test
    void evaluate_returnsDumbMoney_whenHighVolLowConfidence() {
        Stock s = Stock.builder()
                .symbol("RISKY")
                .volatility(0.5)
                .confidenceScore(50)
                .build();

        DumbMoneySignal sig = dumbMoneyService.evaluate(s);

        assertEquals(DumbMoneySignal.DUMB_MONEY, sig);
    }

    @Test
    void evaluate_returnsSmartMoney_whenLowVolHighConfidence() {
        Stock s = Stock.builder()
                .symbol("SAFE")
                .volatility(0.1)
                .confidenceScore(90)
                .build();

        DumbMoneySignal sig = dumbMoneyService.evaluate(s);

        assertEquals(DumbMoneySignal.SMART_MONEY, sig);
    }

    @Test
    void generateHeatmap_returnsSignalsForRepositoryStocks() {
        Stock a = Stock.builder().symbol("A").volatility(0.4).confidenceScore(60).build();
        Stock b = Stock.builder().symbol("B").volatility(0.2).confidenceScore(90).build();

        when(stockRepository.findAll()).thenReturn(List.of(a, b));

        List<DumbMoneySignal> heatmap = dumbMoneyService.generateHeatmap();

        assertEquals(2, heatmap.size());
        assertTrue(heatmap.contains(DumbMoneySignal.DUMB_MONEY));
        assertTrue(heatmap.contains(DumbMoneySignal.SMART_MONEY) || heatmap.contains(DumbMoneySignal.NEUTRAL));

        verify(stockRepository, times(1)).findAll();
    }
}
