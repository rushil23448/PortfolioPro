package com.hsbc.portfolio.service;

import com.hsbc.portfolio.dto.MarketMoversDTO;
import com.hsbc.portfolio.dto.StockDTO;
import com.hsbc.portfolio.entity.Stock;
import com.hsbc.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final StockRepository stockRepository;

    public MarketMoversDTO getMarketMovers() {
        List<Stock> stocks = stockRepository.findAll();
        List<StockDTO> stockDTOs = stocks.stream()
                .map(this::toStockDTO)
                .filter(dto -> dto.getChangePercent() != null)
                .collect(Collectors.toList());

        List<StockDTO> topGainers = stockDTOs.stream()
                .sorted(Comparator.comparing(StockDTO::getChangePercent).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<StockDTO> topLosers = stockDTOs.stream()
                .sorted(Comparator.comparing(StockDTO::getChangePercent))
                .limit(5)
                .collect(Collectors.toList());

        List<StockDTO> mostActive = stockDTOs.stream()
                .filter(s -> s.getVolume() != null)
                .sorted(Comparator.comparing(StockDTO::getVolume).reversed())
                .limit(5)
                .collect(Collectors.toList());

        MarketMoversDTO.MarketSummary summary = getMarketSummary(stockDTOs);

        return MarketMoversDTO.builder()
                .topGainers(topGainers)
                .topLosers(topLosers)
                .mostActive(mostActive)
                .summary(summary)
                .build();
    }

    public List<StockDTO> getTopGainers(int limit) {
        List<Stock> stocks = stockRepository.findAll();
        return stocks.stream()
                .map(this::toStockDTO)
                .filter(dto -> dto.getChangePercent() != null)
                .sorted(Comparator.comparing(StockDTO::getChangePercent).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<StockDTO> getTopLosers(int limit) {
        List<Stock> stocks = stockRepository.findAll();
        return stocks.stream()
                .map(this::toStockDTO)
                .filter(dto -> dto.getChangePercent() != null)
                .sorted(Comparator.comparing(StockDTO::getChangePercent))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<StockDTO> getMostActive(int limit) {
        List<Stock> stocks = stockRepository.findAll();
        return stocks.stream()
                .map(this::toStockDTO)
                .filter(dto -> dto.getVolume() != null)
                .sorted(Comparator.comparing(StockDTO::getVolume).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public MarketMoversDTO.MarketSummary getMarketSummary() {
        List<Stock> stocks = stockRepository.findAll();
        List<StockDTO> stockDTOs = stocks.stream()
                .map(this::toStockDTO)
                .collect(Collectors.toList());
        return getMarketSummary(stockDTOs);
    }

    public MarketMoversDTO.MarketSummary getMarketSummary(List<StockDTO> stocks) {
        if (stocks.isEmpty()) {
            return MarketMoversDTO.MarketSummary.builder()
                    .totalStocks(0)
                    .advancingStocks(0)
                    .decliningStocks(0)
                    .marketSentiment(50.0)
                    .overallTrend("NEUTRAL")
                    .lastUpdated(LocalDateTime.now().toString())
                    .build();
        }

        long advancing = stocks.stream().filter(s -> s.getChangePercent() != null && s.getChangePercent() > 0).count();
        long declining = stocks.stream().filter(s -> s.getChangePercent() != null && s.getChangePercent() < 0).count();

        double avgChange = stocks.stream()
                .filter(s -> s.getChangePercent() != null)
                .mapToDouble(StockDTO::getChangePercent)
                .average()
                .orElse(0);

        double sentimentScore = calculateMarketSentiment(advancing, declining, avgChange);
        String overallTrend = determineOverallTrend(avgChange, sentimentScore);

        return MarketMoversDTO.MarketSummary.builder()
                .totalStocks(stocks.size())
                .advancingStocks((int) advancing)
                .decliningStocks((int) declining)
                .marketSentiment(sentimentScore)
                .overallTrend(overallTrend)
                .lastUpdated(LocalDateTime.now().toString())
                .build();
    }

    private double calculateMarketSentiment(long advancing, long declining, double avgChange) {
        if (advancing + declining == 0) return 50.0;

        double advancingRatio = (double) advancing / (advancing + declining);
        double sentimentFromAdvance = advancingRatio * 60;
        double sentimentFromAvgChange = Math.min(100, Math.max(0, 50 + avgChange));
        
        return Math.round((sentimentFromAdvance + sentimentFromAvgChange) * 100) / 100.0;
    }

    private String determineOverallTrend(double avgChange, double sentiment) {
        if (avgChange > 1.5 && sentiment > 60) return "BULLISH";
        if (avgChange < -1.5 && sentiment < 40) return "BEARISH";
        if (avgChange > 0.5 && sentiment > 55) return "SLIGHTLY_BULLISH";
        if (avgChange < -0.5 && sentiment < 45) return "SLIGHTLY_BEARISH";
        return "NEUTRAL";
    }

    private StockDTO toStockDTO(Stock stock) {
        Double marketCapValue = null;
        if (stock.getMarketCap() != null) {
            marketCapValue = stock.getMarketCap().doubleValue();
        }
        
        BigDecimal currentPriceValue = stock.getCurrentPrice() != null 
                ? stock.getCurrentPrice() 
                : BigDecimal.ZERO;
        
        return StockDTO.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .sector(stock.getSector())
                .exchange(stock.getExchange())
                .currentPrice(currentPriceValue)
                .changePercent(stock.getChangePercent())
                .volume(stock.getVolume())
                .peRatio(stock.getPeRatio())
                .marketCap(marketCapValue)
                .build();
    }
}

