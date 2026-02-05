package com.example.portfolio_management_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    private String symbol;

    private String name;
    private String sector;

    private Double basePrice;
    private Double volatility;
    private Integer confidenceScore;

    private Double currentPrice;

    // 🔥 Dumb Money Indicator
    @Enumerated(EnumType.STRING)
    private DumbMoneySignal dumbMoneySignal;
}
