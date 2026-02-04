package com.hsbc.portfolio.service;

import com.hsbc.portfolio.dto.SectorPerformanceDTO;
import com.hsbc.portfolio.entity.Stock;
import com.hsbc.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectorAnalysisService {

    private final StockRepository stockRepository;

    private static final Map<String, String> SECTOR_DISPLAY_NAMES = Map.ofEntries(
        Map.entry("Technology", "Technology"),
        Map.entry("Finance", "Finance"),
        Map.entry("Healthcare", "Healthcare"),
        Map.entry("Energy", "Energy"),
        Map.entry("Consumer Goods", "Consumer Goods"),
        Map.entry("Consumer Discretionary", "Consumer Discretionary"),
        Map.entry("Industrials", "Industrials"),
        Map.entry("Materials", "Materials"),
        Map.entry("Utilities", "Utilities"),
        Map.entry("Real Estate", "Real Estate"),
        Map.entry("Communication Services", "Communication"),
        Map.entry("Other", "Other")
    );

    private static final Map<String, Double> SECTOR_WEEK_CHANGES = Map.ofEntries(
        Map.entry("Technology", 2.5),
        Map.entry("Finance", 1.8),
        Map.entry("Healthcare", -0.5),
        Map.entry("Energy", -1.2),
        Map.entry("Consumer Goods", 0.8),
        Map.entry("Consumer Discretionary", 1.5),
        Map.entry("Industrials", 0.3),
        Map.entry("Materials", -0.8),
        Map.entry("Utilities", 0.1),
        Map.entry("Real Estate", -0.3),
        Map.entry("Communication Services", 1.2),
        Map.entry("Other", 0.5)
    );

    private static final Map<String, Double> SECTOR_MONTH_CHANGES = Map.ofEntries(
        Map.entry("Technology", 8.5),
        Map.entry("Finance", 5.2),
        Map.entry("Healthcare", 2.1),
        Map.entry("Energy", -3.5),
        Map.entry("Consumer Goods", 3.8),
        Map.entry("Consumer Discretionary", 6.2),
        Map.entry("Industrials", 4.1),
        Map.entry("Materials", -1.5),
        Map.entry("Utilities", 1.2),
        Map.entry("Real Estate", -2.8),
        Map.entry("Communication Services", 4.5),
        Map.entry("Other", 2.5)
    );

    public SectorPerformanceDTO getSectorPerformance() {
        List<Stock> stocks = stockRepository.findAll();
        
        Map<String, List<Stock>> stocksBySector = stocks.stream()
                .collect(Collectors.groupingBy(s -> s.getSector() != null ? s.getSector() : "Other"));

        List<SectorPerformanceDTO.SectorData> sectorDataList = new ArrayList<>();
        
        for (Map.Entry<String, List<Stock>> entry : stocksBySector.entrySet()) {
            String sector = entry.getKey();
            List<Stock> sectorStocks = entry.getValue();
            
            double avgChange = sectorStocks.stream()
                    .filter(s -> s.getChangePercent() != null)
                    .mapToDouble(Stock::getChangePercent)
                    .average()
                    .orElse(0);
            
            long stockCount = sectorStocks.size();
            double totalMarketCap = sectorStocks.stream()
                    .filter(s -> s.getMarketCap() != null)
                    .mapToDouble(s -> s.getMarketCap().doubleValue())
                    .sum();
            double avgPE = sectorStocks.stream()
                    .filter(s -> s.getPeRatio() != null)
                    .mapToDouble(Stock::getPeRatio)
                    .average()
                    .orElse(20.0);
            
            String sentiment = determineSectorSentiment(avgChange);
            double sentimentScore = calculateSectorSentimentScore(avgChange);
            String aiInsight = generateSectorInsight(sector, avgChange, sentiment);
            
            SectorPerformanceDTO.SectorData data = SectorPerformanceDTO.SectorData.builder()
                    .name(sector)
                    .displayName(SECTOR_DISPLAY_NAMES.getOrDefault(sector, sector))
                    .dayChange(Math.round(avgChange * 100) / 100.0)
                    .dayChangePercent(Math.round(avgChange * 100) / 100.0)
                    .weekChange(SECTOR_WEEK_CHANGES.getOrDefault(sector, 0.0))
                    .monthChange(SECTOR_MONTH_CHANGES.getOrDefault(sector, 0.0))
                    .stockCount((int) stockCount)
                    .totalMarketCap(totalMarketCap)
                    .avgPE(Math.round(avgPE * 10) / 10.0)
                    .sentiment(sentiment)
                    .sentimentScore(sentimentScore)
                    .aiInsight(aiInsight)
                    .build();
            
            sectorDataList.add(data);
        }

        // Sort by day change descending
        sectorDataList.sort(Comparator.comparing(SectorPerformanceDTO.SectorData::getDayChange).reversed());

        SectorPerformanceDTO.MarketSentiment marketSentiment = getMarketSentiment();

        return SectorPerformanceDTO.builder()
                .sectors(sectorDataList)
                .marketSentiment(marketSentiment)
                .build();
    }

    public SectorPerformanceDTO.SectorData getSectorByName(String sectorName) {
        SectorPerformanceDTO dto = getSectorPerformance();
        return dto.getSectors().stream()
                .filter(s -> s.getName().equalsIgnoreCase(sectorName))
                .findFirst()
                .orElse(null);
    }

    public SectorPerformanceDTO.MarketSentiment getMarketSentiment() {
        List<Stock> stocks = stockRepository.findAll();
        
        if (stocks.isEmpty()) {
            return SectorPerformanceDTO.MarketSentiment.builder()
                    .overallScore(50.0)
                    .overallTrend("NEUTRAL")
                    .summary("No market data available")
                    .keyThemes("N/A")
                    .build();
        }
        
        long advancing = stocks.stream().filter(s -> s.getChangePercent() != null && s.getChangePercent() > 0).count();
        long declining = stocks.stream().filter(s -> s.getChangePercent() != null && s.getChangePercent() < 0).count();
        
        double avgChange = stocks.stream()
                .filter(s -> s.getChangePercent() != null)
                .mapToDouble(Stock::getChangePercent)
                .average()
                .orElse(0);
        
        double overallScore = Math.min(100, Math.max(0, 50 + avgChange * 5));
        
        String overallTrend;
        if (avgChange > 1.0) overallTrend = "BULLISH";
        else if (avgChange < -1.0) overallTrend = "BEARISH";
        else overallTrend = "NEUTRAL";
        
        String summary = String.format("Market %s: %.1f%% of stocks advancing",
                overallTrend.toLowerCase(),
                advancing * 100.0 / (advancing + declining));
        
        return SectorPerformanceDTO.MarketSentiment.builder()
                .overallScore(Math.round(overallScore * 10) / 10.0)
                .overallTrend(overallTrend)
                .summary(summary)
                .keyThemes("Sector rotation, Q4 earnings, FII flows")
                .build();
    }

    private String determineSectorSentiment(double change) {
        if (change > 1.5) return "BULLISH";
        if (change > 0.5) return "SLIGHTLY_BULLISH";
        if (change > -0.5) return "NEUTRAL";
        if (change > -1.5) return "SLIGHTLY_BEARISH";
        return "BEARISH";
    }

    private double calculateSectorSentimentScore(double change) {
        return Math.min(100, Math.max(0, 50 + change * 10));
    }

    private String generateSectorInsight(String sector, double change, String sentiment) {
        if (sentiment.contains("BULLISH")) {
            return String.format("%s sector showing strength with %.1f%% gain. Buying interest remains high.",
                    sector, change);
        } else if (sentiment.contains("BEARISH")) {
            return String.format("%s under pressure today, down %.1f%%. Selling observed in the sector.",
                    sector, Math.abs(change));
        } else {
            return String.format("%s trading flat at %.1f%%. No major move in the sector today.",
                    sector, change);
        }
    }
}

