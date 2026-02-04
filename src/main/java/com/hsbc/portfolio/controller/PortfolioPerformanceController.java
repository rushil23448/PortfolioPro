package com.hsbc.portfolio.controller;

import com.hsbc.portfolio.dto.PortfolioPerformanceDTO;
import com.hsbc.portfolio.entity.Portfolio;
import com.hsbc.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for portfolio performance data.
 */
@Slf4j
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioPerformanceController {

    private final PortfolioService portfolioService;

    private static final String DEFAULT_USER = "user1";

    @GetMapping("/performance")
    public ResponseEntity<PortfolioPerformanceDTO> getPerformance(
            @RequestParam(required = false) Long portfolioId) {
        
        log.info("Fetching portfolio performance - portfolioId: {}", portfolioId);
        
        Long pid = portfolioId;
        if (pid == null) {
            Portfolio p = portfolioService.getOrCreateDefault(DEFAULT_USER);
            pid = p.getId();
        }
        
        Portfolio portfolio = portfolioService.getPortfolioById(pid);
        if (portfolio == null) {
            log.warn("Portfolio not found: {}", pid);
            return ResponseEntity.notFound().build();
        }
        
        PortfolioPerformanceDTO performance = portfolioService.calculatePerformance(portfolio);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/performance/history")
    public ResponseEntity<List<PortfolioPerformanceDTO.HistoricalPoint>> getPerformanceHistory(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) Long portfolioId) {
        
        log.info("Fetching portfolio performance history - days: {}, portfolioId: {}", days, portfolioId);
        
        Long pid = portfolioId;
        if (pid == null) {
            Portfolio p = portfolioService.getOrCreateDefault(DEFAULT_USER);
            pid = p.getId();
        }
        
        Portfolio portfolio = portfolioService.getPortfolioById(pid);
        if (portfolio == null) {
            log.warn("Portfolio not found: {}", pid);
            return ResponseEntity.notFound().build();
        }
        
        List<PortfolioPerformanceDTO.HistoricalPoint> history = 
                portfolioService.getPerformanceHistory(portfolio, days);
        
        return ResponseEntity.ok(history);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(required = false) Long portfolioId) {
        
        Long pid = portfolioId;
        if (pid == null) {
            Portfolio p = portfolioService.getOrCreateDefault(DEFAULT_USER);
            pid = p.getId();
        }
        
        Portfolio portfolio = portfolioService.getPortfolioById(pid);
        if (portfolio == null) {
            return ResponseEntity.notFound().build();
        }
        
        PortfolioPerformanceDTO performance = portfolioService.calculatePerformance(portfolio);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalValue", performance.getTotalValue());
        summary.put("totalGain", performance.getTotalGain());
        summary.put("totalGainPercent", performance.getTotalGainPercent());
        summary.put("dayChange", performance.getDayChange());
        summary.put("dayChangePercent", performance.getDayChangePercent());
        summary.put("holdingCount", performance.getHoldings() != null ? performance.getHoldings().size() : 0);
        
        return ResponseEntity.ok(summary);
    }
}

