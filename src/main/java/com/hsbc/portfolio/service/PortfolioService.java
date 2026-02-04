package com.hsbc.portfolio.service;

import com.hsbc.portfolio.dto.PortfolioDiversificationDTO;
import com.hsbc.portfolio.dto.PortfolioPerformanceDTO;
import com.hsbc.portfolio.entity.Portfolio;
import com.hsbc.portfolio.entity.PortfolioHolding;
import com.hsbc.portfolio.entity.Stock;
import com.hsbc.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final StockService stockService;

    public Portfolio getOrCreateDefault(String userId) {
        return portfolioRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseGet(() -> {
                    Portfolio p = Portfolio.builder()
                            .userId(userId)
                            .name("My Portfolio")
                            .createdAt(java.time.LocalDateTime.now())
                            .updatedAt(java.time.LocalDateTime.now())
                            .build();
                    return portfolioRepository.save(p);
                });
    }

    public List<PortfolioHolding> getHoldings(Long portfolioId) {
        Portfolio p = portfolioRepository.findById(portfolioId).orElse(null);
        return p != null ? p.getHoldings() : Collections.emptyList();
    }

    public PortfolioDiversificationDTO getDiversification(Long portfolioId) {
        List<PortfolioHolding> holdings = getHoldings(portfolioId);
        if (holdings.isEmpty()) {
            return PortfolioDiversificationDTO.builder()
                    .diversificationScore(0.0)
                    .sectorAllocation(Collections.emptyMap())
                    .exchangeAllocation(Collections.emptyMap())
                    .suggestions(List.of("Add stocks to see diversification.", "Aim for 5+ sectors and both BSE/NSE."))
                    .build();
        }

        BigDecimal totalValue = holdings.stream()
                .map(h -> h.getCurrentValue() != null ? h.getCurrentValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Double> sectorAlloc = new HashMap<>();
        Map<String, Double> exchangeAlloc = new HashMap<>();

        for (PortfolioHolding h : holdings) {
            Stock s = h.getStock();
            BigDecimal val = h.getCurrentValue() != null ? h.getCurrentValue() : BigDecimal.ZERO;
            double pct = totalValue.doubleValue() > 0 ? val.doubleValue() / totalValue.doubleValue() * 100 : 0;
            String sector = s.getSector() != null ? s.getSector() : "Other";
            String exchange = s.getExchange() != null ? s.getExchange() : "NSE";
            sectorAlloc.merge(sector, pct, Double::sum);
            exchangeAlloc.merge(exchange, pct, Double::sum);
        }

        int sectorCount = sectorAlloc.size();
        int exchangeCount = exchangeAlloc.size();
        double maxSectorPct = sectorAlloc.values().stream().max(Double::compare).orElse(0.0);
        double divScore = Math.min(100, sectorCount * 12 + (maxSectorPct < 50 ? 20 : 0) + exchangeCount * 10);

        List<String> suggestions = new ArrayList<>();
        if (sectorCount < 3) suggestions.add("Add stocks from more sectors (aim for 3–5) to reduce sector risk.");
        if (maxSectorPct > 60) suggestions.add("Reduce concentration in your largest sector.");
        if (exchangeCount < 2) suggestions.add("Consider adding stocks from both BSE and NSE for diversification.");

        return PortfolioDiversificationDTO.builder()
                .diversificationScore(divScore)
                .sectorAllocation(sectorAlloc)
                .exchangeAllocation(exchangeAlloc)
                .suggestions(suggestions)
                .build();
    }

    public Portfolio getPortfolioById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId).orElse(null);
    }

    public PortfolioPerformanceDTO calculatePerformance(Portfolio portfolio) {
        List<PortfolioHolding> holdings = portfolio.getHoldings();
        if (holdings.isEmpty()) {
            return PortfolioPerformanceDTO.builder()
                    .totalValue(0.0)
                    .totalGain(0.0)
                    .totalGainPercent(0.0)
                    .dayChange(0.0)
                    .dayChangePercent(0.0)
                    .history(List.of())
                    .holdings(List.of())
                    .build();
        }

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal dayChange = BigDecimal.ZERO;

        for (PortfolioHolding h : holdings) {
            Stock s = h.getStock();
            BigDecimal currentVal = h.getCurrentValue() != null ? h.getCurrentValue() : BigDecimal.ZERO;
            BigDecimal avgPrice = h.getAvgBuyPrice() != null ? h.getAvgBuyPrice() : BigDecimal.ZERO;
            Integer qty = h.getQuantity() != null ? h.getQuantity() : 0;
            BigDecimal costBasis = avgPrice.multiply(BigDecimal.valueOf(qty));
            BigDecimal stockDayChange = s.getChangePercent() != null 
                    ? currentVal.multiply(BigDecimal.valueOf(s.getChangePercent() / 100))
                    : BigDecimal.ZERO;

            totalValue = totalValue.add(currentVal);
            totalCost = totalCost.add(costBasis);
            dayChange = dayChange.add(stockDayChange);
        }

        double totalGain = totalValue.doubleValue() - totalCost.doubleValue();
        double totalGainPercent = totalCost.doubleValue() > 0 
                ? (totalGain / totalCost.doubleValue()) * 100 
                : 0;
        double dayChangePercent = totalValue.doubleValue() > 0 
                ? (dayChange.doubleValue() / totalValue.doubleValue()) * 100 
                : 0;

        List<PortfolioPerformanceDTO.HoldingPerformance> holdingsPerf = getHoldingsPerformance(portfolio);
        List<PortfolioPerformanceDTO.HistoricalPoint> history = getPerformanceHistory(portfolio, 30);

        return PortfolioPerformanceDTO.builder()
                .totalValue(totalValue.doubleValue())
                .totalGain(Math.round(totalGain * 100) / 100.0)
                .totalGainPercent(Math.round(totalGainPercent * 100) / 100.0)
                .dayChange(Math.round(dayChange.doubleValue() * 100) / 100.0)
                .dayChangePercent(Math.round(dayChangePercent * 100) / 100.0)
                .history(history)
                .holdings(holdingsPerf)
                .build();
    }

    public List<PortfolioPerformanceDTO.HistoricalPoint> getPerformanceHistory(Portfolio portfolio, int days) {
        List<PortfolioPerformanceDTO.HistoricalPoint> history = new ArrayList<>();
        Random random = new Random(portfolio.getId());
        double baseValue = portfolio.getHoldings().stream()
                .mapToDouble(h -> h.getCurrentValue() != null ? h.getCurrentValue().doubleValue() : 0)
                .sum();

        for (int i = days; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            double variance = (random.nextDouble() - 0.5) * 0.04 * (days - i) / days;
            double value = baseValue * (1 + variance);
            double gain = value - baseValue;
            double gainPercent = baseValue > 0 ? (gain / baseValue) * 100 : 0;

            history.add(PortfolioPerformanceDTO.HistoricalPoint.builder()
                    .date(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .value(Math.round(value * 100) / 100.0)
                    .gain(Math.round(gain * 100) / 100.0)
                    .gainPercent(Math.round(gainPercent * 100) / 100.0)
                    .build());
        }

        return history;
    }

    public List<PortfolioPerformanceDTO.HoldingPerformance> getHoldingsPerformance(Portfolio portfolio) {
        List<PortfolioHolding> holdings = portfolio.getHoldings();
        BigDecimal totalValue = holdings.stream()
                .map(h -> h.getCurrentValue() != null ? h.getCurrentValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return holdings.stream()
                .map(h -> {
                    Stock s = h.getStock();
                    BigDecimal currentVal = h.getCurrentValue() != null ? h.getCurrentValue() : BigDecimal.ZERO;
                    BigDecimal avgPrice = h.getAvgBuyPrice() != null ? h.getAvgBuyPrice() : BigDecimal.ZERO;
                    Integer qty = h.getQuantity() != null ? h.getQuantity() : 0;
                    BigDecimal costBasis = avgPrice.multiply(BigDecimal.valueOf(qty));
                    double gain = currentVal.doubleValue() - costBasis.doubleValue();
                    double gainPercent = costBasis.doubleValue() > 0 ? (gain / costBasis.doubleValue()) * 100 : 0;
                    double dayChange = s.getChangePercent() != null 
                            ? currentVal.doubleValue() * s.getChangePercent() / 100 
                            : 0;
                    double dayChangePercent = s.getChangePercent() != null ? s.getChangePercent() : 0;
                    double weight = totalValue.doubleValue() > 0 
                            ? (currentVal.doubleValue() / totalValue.doubleValue()) * 100 
                            : 0;

                    return PortfolioPerformanceDTO.HoldingPerformance.builder()
                            .symbol(s.getSymbol())
                            .name(s.getName())
                            .currentValue(currentVal.doubleValue())
                            .costBasis(costBasis.doubleValue())
                            .gain(Math.round(gain * 100) / 100.0)
                            .gainPercent(Math.round(gainPercent * 100) / 100.0)
                            .dayChange(Math.round(dayChange * 100) / 100.0)
                            .dayChangePercent(Math.round(dayChangePercent * 100) / 100.0)
                            .weight(Math.round(weight * 100) / 100.0)
                            .exchange(s.getExchange())
                            .build();
                })
                .sorted(Comparator.comparing(PortfolioPerformanceDTO.HoldingPerformance::getCurrentValue).reversed())
                .collect(Collectors.toList());
    }
}
