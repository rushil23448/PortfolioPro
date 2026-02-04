package com.example.portfolio_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PortfolioSummary {

    private Double totalInvested;
    private Double currentValue;
    private Double profitLoss;
}
