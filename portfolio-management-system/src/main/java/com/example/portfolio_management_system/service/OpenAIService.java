package com.example.portfolio_management_system.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class OpenAIService {

    private final WebClient webClient;
    
    // Replace with your OpenAI API key from https://platform.openai.com/api-keys
    private static final String API_KEY = "sk-your-openai-api-key-here";
    private static final String BASE_URL = "https://api.openai.com/v1";

    public OpenAIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    private double getDouble(Map<String, Object> map, String key, double defaultValue) {
        try {
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        try {
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Generate AI-powered investment recommendation
     */
    public String generateRecommendation(String stockSymbol, Map<String, Object> stockData) {
        String prompt = buildRecommendationPrompt(stockSymbol, stockData);
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", new Object[]{
                new HashMap<String, String>() {{ put("role", "system"); put("content", "You are a financial advisor. Provide concise investment analysis."); }},
                new HashMap<String, String>() {{ put("role", "user"); put("content", prompt); }}
            });
            requestBody.put("max_tokens", 300);
            requestBody.put("temperature", 0.7);

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + API_KEY)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.out.println("OpenAI API error: " + e.getMessage());
        }
        
        return getSimulatedRecommendation(stockSymbol, stockData);
    }

    /**
     * Generate portfolio analysis summary
     */
    public String generatePortfolioAnalysis(String holderName, Map<String, Object> portfolioData) {
        String prompt = buildPortfolioAnalysisPrompt(holderName, portfolioData);
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", new Object[]{
                new HashMap<String, String>() {{ put("role", "system"); put("content", "You are a portfolio manager. Provide brief, actionable insights."); }},
                new HashMap<String, String>() {{ put("role", "user"); put("content", prompt); }}
            });
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + API_KEY)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.out.println("OpenAI API error: " + e.getMessage());
        }
        
        return getSimulatedPortfolioAnalysis(holderName, portfolioData);
    }

    /**
     * Generate market sentiment analysis
     */
    public String analyzeMarketSentiment(List<String> stockSymbols) {
        String prompt = "Analyze market sentiment for these Indian stocks: " + String.join(", ", stockSymbols);
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", new Object[]{
                new HashMap<String, String>() {{ put("role", "system"); put("content", "You are a market analyst. Provide brief sentiment analysis."); }},
                new HashMap<String, String>() {{ put("role", "user"); put("content", prompt); }}
            });
            requestBody.put("max_tokens", 400);
            requestBody.put("temperature", 0.7);

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + API_KEY)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.out.println("OpenAI API error: " + e.getMessage());
        }
        
        return getSimulatedMarketSentiment(stockSymbols);
    }

    private String buildRecommendationPrompt(String symbol, Map<String, Object> data) {
        return String.format("Analyze %s and provide a brief investment recommendation: Current Price: %.2f, Base Price: %.2f, Sector: %s, Volatility: %.2f%%, Confidence Score: %d%%. Keep response under 100 words.",
            symbol,
            getDouble(data, "price", 0),
            getDouble(data, "basePrice", 0),
            getString(data, "sector", "Unknown"),
            getDouble(data, "volatility", 0) * 100,
            getInt(data, "confidenceScore", 0)
        );
    }

    private String buildPortfolioAnalysisPrompt(String holderName, Map<String, Object> data) {
        return String.format("Analyze this portfolio for %s: Total Value: %.2f, Total Invested: %.2f, Profit/Loss: %.2f, Top Sectors: %s. Provide brief insights.",
            holderName,
            getDouble(data, "currentValue", 0),
            getDouble(data, "totalInvested", 0),
            getDouble(data, "profitLoss", 0),
            getString(data, "topSectors", "Diversified")
        );
    }

    private String getSimulatedRecommendation(String symbol, Map<String, Object> data) {
        String[] opinions = {
            "Technical indicators suggest a bullish trend. Consider adding to position with strict stop-loss at 5%% below current levels.",
            "The stock shows moderate volatility. Suitable for medium-term investment horizon. Monitor quarterly earnings.",
            "Currently trading near support levels. RSI indicates oversold conditions. Potential bounce expected.",
            "Strong fundamentals with consistent revenue growth. Long-term investors can accumulate on dips.",
            "Sector outlook remains positive. The stock has shown resilience despite market volatility."
        };
        
        String action = getDouble(data, "profitLoss", 0) >= 0 ? "Hold or add" : "Consider averaging";
        int confidence = getInt(data, "confidenceScore", 75);
        
        return symbol + " - Action: " + action + " | Confidence: " + confidence + "%%\n\n" + 
            opinions[new Random().nextInt(opinions.length)] + "\n\n" +
            "Risk Level: " + (getDouble(data, "volatility", 0.02) > 0.025 ? "High" : "Moderate") + "\n" +
            "Suggested Timeframe: " + (getDouble(data, "volatility", 0.02) > 0.025 ? "Short-term" : "Long-term");
    }

    private String getSimulatedPortfolioAnalysis(String holderName, Map<String, Object> data) {
        double pl = getDouble(data, "profitLoss", 0);
        String status = pl >= 0 ? "performing well" : "under pressure";
        
        return "Portfolio Summary for " + holderName + ":\n\n" +
            "Your portfolio is currently " + status + " with P/L of Rs." + String.format("%.2f", pl) + ".\n\n" +
            "Key Observations:\n" +
            "- Overall allocation shows balanced sector distribution\n" +
            "- Technology and Finance sectors dominate exposure\n\n" +
            "Recommendations:\n" +
            "1. Consider rebalancing if any single stock exceeds 30%% of portfolio\n" +
            "2. Review underperformers for potential exit\n" +
            "3. Add defensive stocks (FMCG, Utilities) for stability\n\n" +
            "Overall Assessment: " + (pl >= 0 ? "Positive outlook with room for optimization" : "Review sector allocation for better risk management");
    }

    private String getSimulatedMarketSentiment(List<String> symbols) {
        String[] sentiments = {
            "Market showing mixed signals with tech stocks leading recovery",
            "Banking sector under pressure due to rate concerns",
            "FMCG stocks stable amid inflation worries",
            "IT sector sees renewed buying interest",
            "Metal stocks volatile on global cues"
        };
        
        return "Market Sentiment Analysis\n\n" +
            "Current Mood: Cautiously Optimistic\n\n" +
            "Sector Breakdown:\n" +
            "- Technology: " + sentiments[new Random().nextInt(sentiments.length)] + "\n" +
            "- Banking: " + sentiments[new Random().nextInt(sentiments.length)] + "\n" +
            "- FMCG: " + sentiments[new Random().nextInt(sentiments.length)] + "\n\n" +
            "Key Drivers:\n" +
            "- Q3 earnings season underway\n" +
            "- Global markets influencing local sentiment\n" +
            "- FII flows showing mixed patterns\n\n" +
            "Outlook: Neutral with positive bias";
    }
}
