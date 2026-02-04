package com.hsbc.portfolio.client;

import com.hsbc.portfolio.config.AlphaVantageConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Fetches real-time quote from Alpha Vantage GLOBAL_QUOTE.
 * Also fetches NEWS_SENTIMENT for dumb money analysis.
 * Indian symbols: use SYMBOL.BSE or SYMBOL.NSE (e.g. RELIANCE.BSE, TCS.NSE).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlphaVantageClient {

    private static final String FUNCTION_QUOTE = "GLOBAL_QUOTE";
    private static final String FUNCTION_NEWS = "NEWS_SENTIMENT";

    private final RestTemplate restTemplate;
    private final AlphaVantageConfig config;

    /**
     * Fetch quote for Alpha Vantage symbol (e.g. IBM, RELIANCE.BSE, TCS.NSE).
     */
    @SuppressWarnings("unchecked")
    public QuoteResult fetchQuote(String alphaVantageSymbol) {
        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Alpha Vantage API key not set. Set alpha.vantage.api-key or ALPHA_VANTAGE_API_KEY.");
            return null;
        }
        String url = config.getBaseUrl() + "?function=" + FUNCTION_QUOTE + "&symbol=" + alphaVantageSymbol + "&apikey=" + apiKey;
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) return null;
            if (response.containsKey("Error Message")) {
                log.warn("Alpha Vantage error for {}: {}", alphaVantageSymbol, response.get("Error Message"));
                return null;
            }
            if (response.containsKey("Information")) {
                log.debug("Alpha Vantage info: {}", response.get("Information"));
                return null;
            }
            Object globalQuote = response.get("Global Quote");
            if (!(globalQuote instanceof Map)) return null;
            Map<String, String> quote = (Map<String, String>) globalQuote;
            return mapToQuoteResult(quote);
        } catch (Exception e) {
            log.warn("Alpha Vantage request failed for {}: {}", alphaVantageSymbol, e.getMessage());
            return null;
        }
    }

    /**
     * Fetch news and sentiment for a stock.
     * Returns list of news headlines for AI sentiment analysis.
     */
    @SuppressWarnings("unchecked")
    public NewsSentimentResult fetchNewsSentiment(String symbol) {
        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Alpha Vantage API key not set. Set alpha.vantage.api-key or ALPHA_VANTAGE_API_KEY.");
            return null;
        }
        // Alpha Vantage News Sentiment endpoint
        String url = config.getBaseUrl() + "?function=" + FUNCTION_NEWS 
                + "&tickers=" + symbol 
                + "&limit=10" // Get last 10 news articles
                + "&apikey=" + apiKey;
        
        try {
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            if (response == null || response.isEmpty()) {
                log.debug("No news found for symbol: {}", symbol);
                return NewsSentimentResult.builder()
                        .symbol(symbol)
                        .headlines(Collections.emptyList())
                        .articles(Collections.emptyList())
                        .build();
            }
            
            List<String> headlines = new ArrayList<>();
            List<NewsArticle> articles = new ArrayList<>();
            double totalSentiment = 0;
            int count = 0;
            
            for (Map<String, Object> item : response) {
                String title = (String) item.get("title");
                String articleUrl = (String) item.get("url");
                String source = (String) item.get("source");
                Object timeObj = item.get("time_published");
                Object sentimentObj = item.get("overall_sentiment_score");
                
                if (title != null && !title.isBlank()) {
                    headlines.add(title);
                    
                    // Parse timestamp
                    LocalDateTime publishedAt = null;
                    if (timeObj != null) {
                        publishedAt = parseTimestamp(timeObj.toString());
                    }
                    
                    // Parse sentiment
                    Double sentimentScore = null;
                    if (sentimentObj != null) {
                        try {
                            sentimentScore = Double.parseDouble(sentimentObj.toString());
                            totalSentiment += sentimentScore;
                            count++;
                        } catch (NumberFormatException ignored) {}
                    }
                    
                    // Determine sentiment label
                    String sentimentLabel = getSentimentLabel(sentimentScore);
                    
                    articles.add(NewsArticle.builder()
                            .title(title)
                            .url(articleUrl)
                            .source(source != null ? source : "Unknown")
                            .publishedAt(publishedAt)
                            .sentimentScore(sentimentScore)
                            .sentimentLabel(sentimentLabel)
                            .build());
                }
            }
            
            double avgSentiment = count > 0 ? totalSentiment / count : 0;
            
            log.debug("Fetched {} news articles for {}, avg sentiment: {}", 
                    articles.size(), symbol, avgSentiment);
            
            return NewsSentimentResult.builder()
                    .symbol(symbol)
                    .headlines(headlines)
                    .articles(articles)
                    .avgSentimentScore(avgSentiment)
                    .articleCount(articles.size())
                    .build();
            
        } catch (Exception e) {
            log.warn("Alpha Vantage news request failed for {}: {}", symbol, e.getMessage());
            return NewsSentimentResult.builder()
                    .symbol(symbol)
                    .headlines(Collections.emptyList())
                    .articles(Collections.emptyList())
                    .build();
        }
    }
    
    private LocalDateTime parseTimestamp(String timeStr) {
        // Alpha Vantage format: "20240515T143000" (YYYYMMDDTHHMMSS)
        try {
            if (timeStr != null && timeStr.length() >= 15) {
                String datePart = timeStr.substring(0, 8);
                String timePart = timeStr.substring(9, 15);
                int year = Integer.parseInt(datePart.substring(0, 4));
                int month = Integer.parseInt(datePart.substring(4, 6));
                int day = Integer.parseInt(datePart.substring(6, 8));
                int hour = Integer.parseInt(timePart.substring(0, 2));
                int minute = Integer.parseInt(timePart.substring(2, 4));
                int second = Integer.parseInt(timePart.substring(4, 6));
                return LocalDateTime.of(year, month, day, hour, minute, second);
            }
        } catch (Exception e) {
            log.debug("Failed to parse timestamp: {}", timeStr);
        }
        return LocalDateTime.now();
    }
    
    private String getSentimentLabel(Double score) {
        if (score == null) return "NEUTRAL";
        if (score >= 0.35) return "BULLISH";
        if (score >= 0.15) return "SOMEWHAT_BULLISH";
        if (score > -0.15) return "NEUTRAL";
        if (score > -0.35) return "SOMEWHAT_BEARISH";
        return "BEARISH";
    }

    private QuoteResult mapToQuoteResult(Map<String, String> q) {
        String priceStr = q.get("05. price");
        String prevCloseStr = q.get("08. previous close");
        String changePctStr = q.get("10. change percent");
        String volStr = q.get("06. volume");
        if (priceStr == null || priceStr.isBlank()) return null;
        BigDecimal price = parseDecimal(priceStr);
        BigDecimal previousClose = parseDecimal(prevCloseStr);
        Double changePercent = null;
        if (changePctStr != null && !changePctStr.isBlank()) {
            changePercent = parseChangePercent(changePctStr);
        }
        Long volume = null;
        if (volStr != null && !volStr.isBlank()) {
            try {
                volume = Long.parseLong(volStr.trim());
            } catch (NumberFormatException ignored) {}
        }
        return QuoteResult.builder()
                .price(price)
                .previousClose(previousClose != null ? previousClose : price)
                .changePercent(changePercent)
                .volume(volume)
                .build();
    }

    private static BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return new BigDecimal(s.trim().replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private static Double parseChangePercent(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Double.parseDouble(s.trim().replace("%", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convert our DB symbol + exchange to Alpha Vantage symbol.
     * Alpha Vantage India: RELIANCE.BSE, TCS.NSE (doc examples).
     */
    public static String toAlphaVantageSymbol(String symbol, String exchange) {
        if (symbol == null || symbol.isBlank()) return null;
        if ("BSE".equalsIgnoreCase(exchange)) return symbol.toUpperCase() + ".BSE";
        if ("NSE".equalsIgnoreCase(exchange)) return symbol.toUpperCase() + ".NSE";
        return symbol.toUpperCase();
    }

    /**
     * Convert stock symbol to Alpha Vantage ticker for news (just symbol without exchange suffix).
     */
    public static String toNewsTicker(String symbol) {
        if (symbol == null || symbol.isBlank()) return null;
        // Remove .BSE or .NSE suffix if present
        if (symbol.contains(".")) {
            return symbol.substring(0, symbol.indexOf("."));
        }
        return symbol.toUpperCase();
    }

    @Getter
    @Builder
    public static class QuoteResult {
        private final BigDecimal price;
        private final BigDecimal previousClose;
        private final Double changePercent;
        private final Long volume;
    }

    @Getter
    @Builder
    public static class NewsSentimentResult {
        private final String symbol;
        private final List<String> headlines;
        private final List<NewsArticle> articles;
        private final Double avgSentimentScore;
        private final Integer articleCount;
    }

    @Getter
    @Builder
    public static class NewsArticle {
        private final String title;
        private final String url;
        private final String source;
        private final LocalDateTime publishedAt;
        private final Double sentimentScore;
        private final String sentimentLabel;
    }
}

