package com.example.portfolio_management_system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    // ✅ NEW FIELD: Current Market Price
    private Double currentPrice;
}
