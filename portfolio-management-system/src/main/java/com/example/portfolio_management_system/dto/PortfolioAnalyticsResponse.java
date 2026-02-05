package com.example.portfolio_management_system.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioAnalyticsResponse {

    private String holderName;

    private Double totalInvested;
    private Double currentValue;
    private Double profitLoss;

    private Integer diversificationScore;
    private Integer riskScore;

    // Frontend fields
    private Integer totalHoldings;
    private Integer uniqueStocks;
    private Double averageReturn;
    private String bestPerformer;

    private Map<String, Double> sectorAllocation;
}

