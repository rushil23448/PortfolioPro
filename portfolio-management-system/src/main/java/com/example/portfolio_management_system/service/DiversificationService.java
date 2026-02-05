package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.dto.DiversificationRecommendation;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.repository.HoldingRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DiversificationService {

    private final HoldingRepository holdingRepository;

    public DiversificationService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    public List<DiversificationRecommendation> analyzeDiversification(Long holderId) {

        List<Holding> holdings = holdingRepository.findByHolderId(holderId);

        Map<String, Double> sectorExposure = new HashMap<>();
        double totalValue = 0;

        for (Holding h : holdings) {
            double value = h.getQuantity() * h.getStock().getCurrentPrice();
            totalValue += value;

            sectorExposure.merge(
                    h.getStock().getSector(),
                    value,
                    Double::sum
            );
        }

        List<DiversificationRecommendation> recommendations = new ArrayList<>();

        for (Map.Entry<String, Double> entry : sectorExposure.entrySet()) {

            double percent = (entry.getValue() / totalValue) * 100;

            if (percent > 45) {
                recommendations.add(
                        DiversificationRecommendation.builder()
                                .message("High exposure to " + entry.getKey() + " sector (" + Math.round(percent) + "%)")
                                .severity("HIGH")
                                .suggestedSector("Banking / FMCG / Utilities")
                                .build()
                );
            } else if (percent > 30) {
                recommendations.add(
                        DiversificationRecommendation.builder()
                                .message("Moderate concentration in " + entry.getKey())
                                .severity("MEDIUM")
                                .suggestedSector("Add defensive sectors")
                                .build()
                );
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add(
                    DiversificationRecommendation.builder()
                            .message("Portfolio is well diversified")
                            .severity("LOW")
                            .suggestedSector("None")
                            .build()
            );
        }

        return recommendations;
    }
}
