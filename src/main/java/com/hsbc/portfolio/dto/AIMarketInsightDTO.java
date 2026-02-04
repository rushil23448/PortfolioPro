package com.hsbc.portfolio.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIMarketInsightDTO {
    
    private String title;
    private String summary;
    private String detailedAnalysis;
    private MarketOutlook outlook;
    private List<TrendingStock> trendingStocks;
    private List<RiskAlert> riskAlerts;
    private List<Opportunity> opportunities;
    private String lastUpdated;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarketOutlook {
        private String overall; // BULLISH, NEUTRAL, BEARISH
        private String shortTerm; // 1-7 days
        private String mediumTerm; // 1-4 weeks
        private String longTerm; // 1-6 months
        private Double confidence; // 0-100
        private List<String> keyFactors;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendingStock {
        private String symbol;
        private String name;
        private String reason;
        private Double sentimentScore;
        private String sentimentDirection; // UP, DOWN, NEUTRAL
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskAlert {
        private String type; // OVERHEATED, OVERSOLD, VOLATILE, SENTIMENT
        private String description;
        private String affectedSymbols;
        private Double riskLevel; // 0-100
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Opportunity {
        private String type; // VALUE, MOMENTUM, SECTOR_ROTATION
        private String description;
        private List<String> suggestedSymbols;
        private Double potentialReturn;
        private String timeFrame;
    }
}

