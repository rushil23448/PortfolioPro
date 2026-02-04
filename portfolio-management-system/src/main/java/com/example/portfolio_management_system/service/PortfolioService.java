package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.dto.PortfolioSummary;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    private final MarketPriceService marketPriceService;
    private final AlphaVantageService alphaVantageService;
    public PortfolioSummary getPortfolioSummary(Long holderId) {

        List<Holding> holdings = holdingRepository.findByHolderId(holderId);

        double totalInvested = 0;
        double currentValue = 0;

        for (Holding h : holdings) {

            double invested = h.getAvgPrice() * h.getQuantity();
            totalInvested += invested;

            double livePrice = alphaVantageService.getLivePrice(h.getStockSymbol());


            currentValue += livePrice * h.getQuantity();
        }

        double profitLoss = currentValue - totalInvested;

        return new PortfolioSummary(totalInvested, currentValue, profitLoss);
    }
}
