package com.example.portfolio_management_system.dto;

import java.util.Map;
import lombok.*;

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

    private Map<String, Double> sectorAllocation;
}
