package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.dto.PortfolioAnalyticsResponse;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.HoldingRepository;
import com.example.portfolio_management_system.repository.HolderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioAnalyticsServiceTest {

    @Mock
    HoldingRepository holdingRepository;

    @Mock
    HolderRepository holderRepository;

    @InjectMocks
    PortfolioAnalyticsService analyticsService;

    @Test
    void getAnalytics_computesExpectedValues_forTwoHoldings() {
        Long holderId = 1L;

        Holder holder = Holder.builder().id(holderId).name("Bob").build();

        Stock s1 = Stock.builder()
                .symbol("AAA")
                .sector("Tech")
                .currentPrice(100.0)
                .volatility(0.1)
                .build();

        Stock s2 = Stock.builder()
                .symbol("BBB")
                .sector("Finance")
                .currentPrice(200.0)
                .volatility(0.2)
                .build();

        Holding h1 = Holding.builder().id(1L).quantity(2).avgPrice(90.0).stock(s1).holder(holder).build();
        Holding h2 = Holding.builder().id(2L).quantity(1).avgPrice(150.0).stock(s2).holder(holder).build();

        when(holderRepository.findById(holderId)).thenReturn(Optional.of(holder));
        when(holdingRepository.findByHolderId(holderId)).thenReturn(List.of(h1, h2));

        PortfolioAnalyticsResponse resp = analyticsService.getAnalytics(holderId);

        // Total Invested = 2*90 + 1*150 = 330
        assertEquals(330.0, resp.getTotalInvested());

        // Current Value = 2*100 + 1*200 = 400
        assertEquals(400.0, resp.getCurrentValue());

        // Profit Loss = 70
        assertEquals(70.0, resp.getProfitLoss());

        // Diversification: 2 sectors -> score = 40
        assertEquals(40, resp.getDiversificationScore());

        // Risk score = average volatility*100 = (0.1*100 + 0.2*100)/2 = 15 -> int cast -> 15
        assertEquals(15, resp.getRiskScore());

        // Sector allocation percentages: Tech = 200/400 = 50.00, Finance = 200/400 = 50.00
        assertEquals(2, resp.getSectorAllocation().size());
        assertEquals(50.0, resp.getSectorAllocation().get("Tech"));
        assertEquals(50.0, resp.getSectorAllocation().get("Finance"));

        verify(holderRepository, times(1)).findById(holderId);
        verify(holdingRepository, times(1)).findByHolderId(holderId);
    }
}
