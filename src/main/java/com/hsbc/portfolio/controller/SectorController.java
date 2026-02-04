package com.hsbc.portfolio.controller;

import com.hsbc.portfolio.dto.SectorPerformanceDTO;
import com.hsbc.portfolio.service.SectorAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sectors")
@RequiredArgsConstructor
public class SectorController {

    private final SectorAnalysisService sectorAnalysisService;

    @GetMapping
    public ResponseEntity<SectorPerformanceDTO> getSectorPerformance() {
        log.info("Fetching sector performance data...");
        SectorPerformanceDTO sectors = sectorAnalysisService.getSectorPerformance();
        return ResponseEntity.ok(sectors);
    }

    @GetMapping("/{sectorName}")
    public ResponseEntity<SectorPerformanceDTO.SectorData> getSectorByName(
            @PathVariable String sectorName) {
        SectorPerformanceDTO.SectorData sector = sectorAnalysisService.getSectorByName(sectorName);
        if (sector == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sector);
    }

    @GetMapping("/sentiment")
    public ResponseEntity<SectorPerformanceDTO.MarketSentiment> getMarketSentiment() {
        SectorPerformanceDTO.MarketSentiment sentiment = sectorAnalysisService.getMarketSentiment();
        return ResponseEntity.ok(sentiment);
    }
}

