package com.hsbc.portfolio.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationDTO {
    private StockDTO stock;
    private String action; // BUY, SELL, HOLD
    private String reason;
    private Double score; // 0-100 confidence
    private BigDecimal targetPrice;
}
