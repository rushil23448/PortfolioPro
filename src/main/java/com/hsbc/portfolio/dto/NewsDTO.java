package com.hsbc.portfolio.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for news articles returned from Alpha Vantage News Sentiment API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDTO {
    private String title;
    private String url;
    private String source;
    private String summary;
    private LocalDateTime publishedAt;
    private Double sentimentScore;
    private String sentimentLabel;
}

