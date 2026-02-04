package com.hsbc.portfolio.controller;

import com.hsbc.portfolio.dto.RecommendationDTO;
import com.hsbc.portfolio.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<RecommendationDTO>> getAll(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(recommendationService.getRecommendations(limit));
    }

    @GetMapping("/buy")
    public ResponseEntity<List<RecommendationDTO>> getBuy(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getBuyRecommendations(limit));
    }

    @GetMapping("/sell")
    public ResponseEntity<List<RecommendationDTO>> getSell(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getSellRecommendations(limit));
    }
}
