package com.example.portfolio_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PortfolioItemDTO {

    private String stockSymbol;
    private String stockName;
    private String sector;

    private Double basePrice;
    private Integer quantity;

    private Double investedValue;
}
