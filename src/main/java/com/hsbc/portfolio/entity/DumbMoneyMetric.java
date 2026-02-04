package com.hsbc.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Tracks "Dumb Money" / Emotional Money flow.
 * High retail inflow + social buzz = overheated stock (likely to correct).
 * 
 * Combines:
 * - Real-time price from Alpha Vantage
 * - Real-time volume from Alpha Vantage
 * - AI sentiment analysis from news headlines
 */
@Entity
@Table(name = "dumb_money_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DumbMoneyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private LocalDate date;

    // Real-time market data
    private Double currentPrice;
    private Double changePercent;
    private Long volume;

    // Component scores (0-100)
    private Double priceScore;
    private Double volumeScore;
    private Double aiSentimentScore;

    /** Retail net buy volume (simulated: higher = more FOMO) 0-100 */
    private Double retailFlowScore;

    /** Social/media buzz score 0-100 */
    private Double buzzScore;

    /** Combined heat: higher = more "dumb money" = higher crash risk */
    private Double heatScore;

    /** Interpretation: OVERHEATED, WARM, NEUTRAL, COOL */
    private String heatLevel;

    // Trend indicators
    private String trend;           // STRONG_UP, UP, STABLE, DOWN, STRONG_DOWN
    private Double trendStrength;   // 0-100

    // Market context
    private String marketCapCategory;  // LARGE, MID, SMALL, MICRO

    // AI Analysis
    private String aiReasoning;
    private String sentimentClassification;  // HYPER, WARM, NEUTRAL, COOL
}
