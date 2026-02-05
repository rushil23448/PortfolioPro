package com.example.portfolio_management_system.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DumbMoneyStock {

    private String symbol;
    private String sector;
    private Double volatility;
    private Integer confidenceScore;
    private String label; // DUMB MONEY / NEUTRAL / SMART MONEY
}
