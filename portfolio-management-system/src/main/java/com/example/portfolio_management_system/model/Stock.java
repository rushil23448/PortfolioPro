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

    // ✅ New Fields
    private Double basePrice;

    private Double volatility;

    private Integer confidenceScore;
}
