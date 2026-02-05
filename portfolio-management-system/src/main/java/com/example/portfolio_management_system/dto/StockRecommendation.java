package com.example.portfolio_management_system.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockRecommendation {

    private String stockSymbol;
    private String stockName;

    private Double buyPrice;
    private Double currentPrice;
    private Double profitLossPercent;

    private Integer confidenceScore;
    private Double volatility;

    private String recommendation; // BUY / HOLD / SELL
    private String reason;
}
