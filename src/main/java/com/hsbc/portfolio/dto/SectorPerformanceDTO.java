package com.hsbc.portfolio.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectorPerformanceDTO {
    
    private List<SectorData> sectors;
    private MarketSentiment marketSentiment;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectorData {
        private String name;
        private String displayName;
        private Double dayChange;
        private Double dayChangePercent;
        private Double weekChange;
        private Double monthChange;
        private Integer stockCount;
        private Double totalMarketCap;
        private Double avgPE;
        private String sentiment; // BULLISH, NEUTRAL, BEARISH
        private Double sentimentScore; // 0-100
        private String aiInsight;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarketSentiment {
        private Double overallScore; // 0-100
        private String overallTrend; // BULLISH, NEUTRAL, BEARISH
        private String summary;
        private String keyThemes;
    }
}

