package com.hsbc.portfolio.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioDiversificationDTO {
    private Double diversificationScore; // 0-100
    private Map<String, Double> sectorAllocation; // sector -> % of portfolio
    private Map<String, Double> exchangeAllocation; // BSE/NSE
    private List<String> suggestions; // diversification tips
}
