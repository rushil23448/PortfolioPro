package com.hsbc.portfolio.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDTO {
    private Long id;
    private String symbol;
    private String name;
    private String sector;
    private String exchange;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private Double changePercent;
    private Long volume;
    private Double peRatio;
    private Double marketCap;
    private LocalDateTime updatedAt;
}
