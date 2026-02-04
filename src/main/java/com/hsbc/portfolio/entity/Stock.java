package com.hsbc.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;

    private String name;
    private String sector;
    private String exchange; // BSE or NSE

    @Column(precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(precision = 15, scale = 2)
    private BigDecimal previousClose;

    private Double changePercent;
    private Long volume;
    private Double peRatio;
    private Double marketCap; // in crores

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
