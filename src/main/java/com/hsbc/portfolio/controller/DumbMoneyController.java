package com.hsbc.portfolio.controller;

import com.hsbc.portfolio.dto.DumbMoneyHeatDTO;
import com.hsbc.portfolio.service.DumbMoneyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dumb-money")
@RequiredArgsConstructor
public class DumbMoneyController {

    private final DumbMoneyService dumbMoneyService;

    @GetMapping("/heat-map")
    public ResponseEntity<List<DumbMoneyHeatDTO>> getHeatMap(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DumbMoneyHeatDTO> list = date != null
                ? dumbMoneyService.getHeatMap(date)
                : dumbMoneyService.getHeatMapToday();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/heat-map/realtime")
    public ResponseEntity<List<DumbMoneyHeatDTO>> getHeatMapRealtime() {
        log.info("Fetching real-time dumb money heat map...");
        List<DumbMoneyHeatDTO> list = dumbMoneyService.getHeatMapRealtime();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/heat-map/with-news")
    public ResponseEntity<List<DumbMoneyHeatDTO>> getHeatMapWithNews() {
        log.info("Fetching real-time dumb money heat map with news...");
        List<DumbMoneyHeatDTO> list = dumbMoneyService.getHeatMapWithNews();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/heat-map/{symbol}")
    public ResponseEntity<DumbMoneyHeatDTO> getHeatScoreForStock(@PathVariable String symbol) {
        DumbMoneyHeatDTO dto = dumbMoneyService.getHeatScoreForStock(symbol);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshHeatMap() {
        log.info("Manual refresh triggered for dumb money heat map...");
        long startTime = System.currentTimeMillis();
        
        List<DumbMoneyHeatDTO> list = dumbMoneyService.getHeatMapRealtime();
        
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("stocksAnalyzed", list.size());
        response.put("durationMs", duration);
        response.put("timestamp", System.currentTimeMillis());
        
        log.info("Refresh completed: {} stocks analyzed in {}ms", list.size(), duration);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<DumbMoneyHeatDTO> list = dumbMoneyService.getHeatMapToday();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalStocks", list.size());
        summary.put("overheated", list.stream().filter(d -> "OVERHEATED".equals(d.getHeatLevel())).count());
        summary.put("warm", list.stream().filter(d -> "WARM".equals(d.getHeatLevel())).count());
        summary.put("neutral", list.stream().filter(d -> "NEUTRAL".equals(d.getHeatLevel())).count());
        summary.put("cool", list.stream().filter(d -> "COOL".equals(d.getHeatLevel())).count());
        summary.put("avgHeatScore", list.stream().mapToDouble(DumbMoneyHeatDTO::getHeatScore).average().orElse(0));
        summary.put("topOverheated", list.stream()
                .filter(d -> "OVERHEATED".equals(d.getHeatLevel()))
                .limit(5)
                .toList());
        
        return ResponseEntity.ok(summary);
    }
}

