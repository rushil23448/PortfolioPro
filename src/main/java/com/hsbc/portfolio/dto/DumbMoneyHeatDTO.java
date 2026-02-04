package com.hsbc.portfolio.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DumbMoneyHeatDTO {
    private Long stockId;
    private String symbol;
    private String name;
    private String sector;
    private String exchange;
    
    // Real-time market data
    private Double currentPrice;
    private Double changePercent;
    private Long volume;
    
    // Dumb Money scores
    private Double heatScore;      // 0-100, higher = more emotional money
    private String heatLevel;      // OVERHEATED, WARM, NEUTRAL, COOL
    private Double retailFlowScore; // Volume-based score (0-100)
    private Double buzzScore;       // Sentiment-based score (0-100)
    private Double priceScore;      // Price-based score (0-100)
    private Double aiSentimentScore; // AI analyzed sentiment (0-100)
    
    // AI Analysis details
    private String aiReasoning;
    private String sentimentClassification; // HYPER, WARM, NEUTRAL, COOL
    
    // Real-time News
    private List<NewsDTO> newsHeadlines;
    
    // Trend indicator
    private String trend;          // UP, DOWN, STABLE
    private Double trendStrength;  // 0-100, how strong the trend
    
    // Market cap for context
    private String marketCapCategory; // LARGE, MID, SMALL
    
    // Last update timestamp
    private LocalDateTime lastUpdated;
}
