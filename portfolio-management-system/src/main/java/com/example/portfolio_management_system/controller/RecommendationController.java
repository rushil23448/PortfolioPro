package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.dto.StockRecommendation;
import com.example.portfolio_management_system.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{holderId}")
    public List<StockRecommendation> getRecommendations(
            @PathVariable Long holderId) {
        return recommendationService.getRecommendations(holderId);
    }
}