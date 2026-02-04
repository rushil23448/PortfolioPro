package com.hsbc.portfolio.service;

import com.hsbc.portfolio.dto.AIMarketInsightDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIInsightService {

    public AIMarketInsightDTO getMarketInsights() {
        log.info("Generating AI market insights...");
        
        AIMarketInsightDTO.MarketOutlook outlook = getMarketOutlook();
        AIMarketInsightDTO.TrendingStock trending = getTrendingStocks();
        AIMarketInsightDTO.RiskAlert risks = getRiskAlerts();
        AIMarketInsightDTO.Opportunity opportunities = getOpportunities();

        return AIMarketInsightDTO.builder()
                .title("Market Intelligence Report")
                .summary("Market shows mixed signals with technology and finance sectors leading gains.")
                .detailedAnalysis("The current market environment presents both opportunities and risks.")
                .outlook(outlook)
                .trendingStocks(trending != null ? List.of(trending) : List.of())
                .riskAlerts(risks != null ? List.of(risks) : List.of())
                .opportunities(opportunities != null ? List.of(opportunities) : List.of())
                .lastUpdated(LocalDateTime.now().toString())
                .build();
    }

    public AIMarketInsightDTO.MarketOutlook getMarketOutlook() {
        return AIMarketInsightDTO.MarketOutlook.builder()
                .overall("NEUTRAL")
                .shortTerm("SLIGHTLY_BULLISH")
                .mediumTerm("NEUTRAL")
                .longTerm("BULLISH")
                .confidence(65.0)
                .keyFactors(Arrays.asList("FII inflow momentum", "Global market cues", "Q4 earnings season"))
                .build();
    }

    public AIMarketInsightDTO.TrendingStock getTrendingStocks() {
        return AIMarketInsightDTO.TrendingStock.builder()
                .symbol("RELIANCE")
                .name("Reliance Industries Ltd.")
                .reason("Strong Q4 results and retail segment growth")
                .sentimentScore(72.0)
                .sentimentDirection("UP")
                .build();
    }

    public AIMarketInsightDTO.RiskAlert getRiskAlerts() {
        return AIMarketInsightDTO.RiskAlert.builder()
                .type("OVERHEATED")
                .description("Several small-cap stocks showing signs of overheating")
                .affectedSymbols("Several small-cap names")
                .riskLevel(68.0)
                .build();
    }

    public AIMarketInsightDTO.Opportunity getOpportunities() {
        return AIMarketInsightDTO.Opportunity.builder()
                .type("SECTOR_ROTATION")
                .description("Financial sector presents value opportunity")
                .suggestedSymbols(Arrays.asList("HDFCBANK", "ICICIBANK", "SBIN"))
                .potentialReturn(15.0)
                .timeFrame("2-3 months")
                .build();
    }
}

