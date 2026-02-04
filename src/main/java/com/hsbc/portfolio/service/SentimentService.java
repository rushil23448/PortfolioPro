package com.hsbc.portfolio.service;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SentimentService {

    private final ChatModel chatModel;

    // Constructor with optional ChatModel
    @Autowired
    public SentimentService(@Autowired(required = false) ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public SentimentAnalysis analyzeHeadlines(String symbol, List<String> headlines) {
        if (headlines == null || headlines.isEmpty()) {
            log.debug("No headlines provided for sentiment analysis of {}", symbol);
            return SentimentAnalysis.builder()
                    .symbol(symbol)
                    .score(50.0)
                    .classification("NEUTRAL")
                    .build();
        }

        // If ChatModel is not configured (no API key), use default
        if (chatModel == null) {
            log.debug("OpenAI not configured, using default sentiment for {}", symbol);
            return SentimentAnalysis.builder()
                    .symbol(symbol)
                    .score(50.0)
                    .classification("NEUTRAL")
                    .reasoning("AI not configured - using default neutral sentiment")
                    .build();
        }

        String headlinesText = String.join("\n- ", headlines);
        String prompt = """
            Analyze these headlines for %s. On a scale of 0-100, how much retail 'FOMO' or emotional hype is present?
            
            Headlines:
            - %s
            
            Consider:
            - Words like "soar", "surge", "explode", "must buy", "don't miss out" indicate HIGH hype
            - Words like "cautious", "declines", "uncertain", "caution" indicate LOW hype
            - Viral/social media style language indicates retail FOMO
            
            Respond ONLY with a JSON object in this format:
            {"score": <number>, "classification": "<HYPER|WARM|NEUTRAL|COOL>", "reasoning": "<brief explanation>"}
            
            Rules:
            - score >= 70: HYPER (extreme retail FOMO, bubble territory)
            - score >= 50: WARM (elevated hype)
            - score >= 30: NEUTRAL (balanced coverage)
            - score < 30: COOL (low interest, possibly negative sentiment)
            """.formatted(symbol, headlinesText);

        try {
            String content = chatModel.call(prompt);
            log.debug("AI Sentiment response for {}: {}", symbol, content);
            return parseResponse(symbol, content);
        } catch (Exception e) {
            log.warn("AI sentiment analysis failed for {}: {}", symbol, e.getMessage());
            return SentimentAnalysis.builder()
                    .symbol(symbol)
                    .score(50.0)
                    .classification("NEUTRAL")
                    .reasoning("AI analysis failed, using default")
                    .build();
        }
    }

    private SentimentAnalysis parseResponse(String symbol, String response) {
        try {
            String scoreStr = extractValue(response, "score");
            String classification = extractValue(response, "classification");
            String reasoning = extractValue(response, "reasoning");

            double score = 50.0;
            if (scoreStr != null) {
                score = Double.parseDouble(scoreStr.replaceAll("[^0-9.]", ""));
            }

            if (classification == null || classification.isBlank()) {
                classification = classify(score);
            }

            return SentimentAnalysis.builder()
                    .symbol(symbol)
                    .score(score)
                    .classification(classification)
                    .reasoning(reasoning != null ? reasoning : "AI analyzed headlines")
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse AI response for {}: {}", symbol, response);
            return SentimentAnalysis.builder()
                    .symbol(symbol)
                    .score(50.0)
                    .classification("NEUTRAL")
                    .reasoning("Parse failed, using default")
                    .build();
        }
    }

    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*";
        int keyStart = json.indexOf(pattern);
        if (keyStart == -1) return null;
        
        int valueStart = keyStart + pattern.length();
        char firstChar = json.charAt(valueStart);
        
        if (firstChar == '"') {
            int valueEnd = json.indexOf("\"", valueStart + 1);
            if (valueEnd == -1) return null;
            return json.substring(valueStart + 1, valueEnd);
        } else {
            int valueEnd = valueStart;
            while (valueEnd < json.length() && (Character.isDigit(json.charAt(valueEnd)) || json.charAt(valueEnd) == '.')) {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd);
        }
    }

    private String classify(double score) {
        if (score >= 70) return "HYPER";
        if (score >= 50) return "WARM";
        if (score >= 30) return "NEUTRAL";
        return "COOL";
    }

    @lombok.Builder
    @lombok.Getter
    public static class SentimentAnalysis {
        private final String symbol;
        private final double score;
        private final String classification;
        private final String reasoning;
    }
}

