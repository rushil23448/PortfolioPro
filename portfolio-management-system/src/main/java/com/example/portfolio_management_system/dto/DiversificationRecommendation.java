package com.example.portfolio_management_system.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiversificationRecommendation {

    private String message;
    private String severity; // LOW, MEDIUM, HIGH
    private String suggestedSector;
}
