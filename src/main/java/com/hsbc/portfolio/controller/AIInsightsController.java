package com.hsbc.portfolio.controller;

import com.hsbc.portfolio.dto.AIMarketInsightDTO;
import com.hsbc.portfolio.service.AIInsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for AI-generated market insights.
 */
@Slf4j
@RestController
@RequestMapping("/api/ai-insights")
@RequiredArgsConstructor
public class AIInsightsController {

    private final AIInsightService aiInsightService;

    @GetMapping
    public ResponseEntity<AIMarketInsightDTO> getInsights() {
        log.info("Fetching AI market insights...");
        AIMarketInsightDTO insights = aiInsightService.getMarketInsights();
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/outlook")
    public ResponseEntity<AIMarketInsightDTO.MarketOutlook> getMarketOutlook() {
        log.info("Fetching market outlook...");
        AIMarketInsightDTO.MarketOutlook outlook = aiInsightService.getMarketOutlook();
        return ResponseEntity.ok(outlook);
    }

    @GetMapping("/trending")
    public ResponseEntity<AIMarketInsightDTO.TrendingStock> getTrendingStocks() {
        log.info("Fetching trending stocks...");
        AIMarketInsightDTO.TrendingStock trending = aiInsightService.getTrendingStocks();
        return ResponseEntity.ok(trending);
    }

    @GetMapping("/risks")
    public ResponseEntity<AIMarketInsightDTO.RiskAlert> getRiskAlerts() {
        log.info("Fetching risk alerts...");
        AIMarketInsightDTO.RiskAlert risks = aiInsightService.getRiskAlerts();
        return ResponseEntity.ok(risks);
    }

    @GetMapping("/opportunities")
    public ResponseEntity<AIMarketInsightDTO.Opportunity> getOpportunities() {
        log.info("Fetching opportunities...");
        AIMarketInsightDTO.Opportunity opportunities = aiInsightService.getOpportunities();
        return ResponseEntity.ok(opportunities);
    }
}

