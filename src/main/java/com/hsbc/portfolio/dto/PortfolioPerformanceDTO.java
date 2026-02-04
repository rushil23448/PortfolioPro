package com.hsbc.portfolio.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioPerformanceDTO {
    
    private Double totalValue;
    private Double totalGain;
    private Double totalGainPercent;
    private Double dayChange;
    private Double dayChangePercent;
    private List<HistoricalPoint> history;
    private List<HoldingPerformance> holdings;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistoricalPoint {
        private String date;
        private Double value;
        private Double gain;
        private Double gainPercent;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HoldingPerformance {
        private String symbol;
        private String name;
        private Double currentValue;
        private Double costBasis;
        private Double gain;
        private Double gainPercent;
        private Double dayChange;
        private Double dayChangePercent;
        private Double weight;
        private String exchange;
    }
}

