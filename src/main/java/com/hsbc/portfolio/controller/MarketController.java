package com.hsbc.portfolio.controller;

import com.hsbc.portfolio.dto.MarketMoversDTO;
import com.hsbc.portfolio.dto.StockDTO;
import com.hsbc.portfolio.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketDataService marketDataService;

    @GetMapping("/movers")
    public ResponseEntity<MarketMoversDTO> getMarketMovers() {
        log.info("Fetching market movers data...");
        MarketMoversDTO movers = marketDataService.getMarketMovers();
        return ResponseEntity.ok(movers);
    }

    @GetMapping("/top-gainers")
    public ResponseEntity<List<StockDTO>> getTopGainers(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(marketDataService.getTopGainers(limit));
    }

    @GetMapping("/top-losers")
    public ResponseEntity<List<StockDTO>> getTopLosers(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(marketDataService.getTopLosers(limit));
    }

    @GetMapping("/most-active")
    public ResponseEntity<List<StockDTO>> getMostActive(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(marketDataService.getMostActive(limit));
    }

    @GetMapping("/overview")
    public ResponseEntity<MarketMoversDTO.MarketSummary> getMarketOverview() {
        MarketMoversDTO.MarketSummary summary = marketDataService.getMarketSummary();
        return ResponseEntity.ok(summary);
    }
}

