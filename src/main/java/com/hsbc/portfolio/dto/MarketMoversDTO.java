package com.hsbc.portfolio.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketMoversDTO {
    
    private List<StockDTO> topGainers;
    private List<StockDTO> topLosers;
    private List<StockDTO> mostActive;
    private MarketSummary summary;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarketSummary {
        private Integer totalStocks;
        private Integer advancingStocks;
        private Integer decliningStocks;
        private Double marketSentiment; // 0-100 scale
        private String overallTrend; // BULLISH, BEARISH, NEUTRAL
        private String lastUpdated;
    }
}

