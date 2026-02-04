package com.hsbc.portfolio.controller;

import com.hsbc.portfolio.client.AlphaVantageClient;
import com.hsbc.portfolio.dto.NewsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST controller for fetching real-time news for stocks.
 */
@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final AlphaVantageClient alphaVantageClient;

    // Sample news headlines for demo purposes when API is unavailable
    private static final Map<String, List<NewsDTO>> DEMO_NEWS = new HashMap<>();
    static {
        // RELIANCE news
        DEMO_NEWS.put("RELIANCE", Arrays.asList(
            NewsDTO.builder().title("Reliance Industries Q4 Results Beat Street Estimates").url("#").source("ET").publishedAt(LocalDateTime.now().minusHours(2)).sentimentScore(0.45).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Reliance Jio Adds 5 Million New Subscribers in Q1").url("#").source("LiveMint").publishedAt(LocalDateTime.now().minusHours(6)).sentimentScore(0.35).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Retail Business Drives Growth for Reliance in FY24").url("#").source("Business Standard").publishedAt(LocalDateTime.now().minusHours(12)).sentimentScore(0.25).sentimentLabel("SOMEWHAT_BULLISH").build()
        ));
        
        // HDFCBANK news
        DEMO_NEWS.put("HDFCBANK", Arrays.asList(
            NewsDTO.builder().title("HDFC Bank Reports Strong Q4 Profit Growth").url("#").source("Economic Times").publishedAt(LocalDateTime.now().minusHours(1)).sentimentScore(0.40).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Digital Banking Initiatives Boost HDFC Bank's Customer Base").url("#").source("Moneycontrol").publishedAt(LocalDateTime.now().minusHours(5)).sentimentScore(0.30).sentimentLabel("SOMEWHAT_BULLISH").build(),
            NewsDTO.builder().title("HDFC Bank Maintains Steady Asset Quality in Q4").url("#").source("Business Today").publishedAt(LocalDateTime.now().minusHours(8)).sentimentScore(0.20).sentimentLabel("NEUTRAL").build()
        ));
        
        // TCS news
        DEMO_NEWS.put("TCS", Arrays.asList(
            NewsDTO.builder().title("TCS Wins Multi-Year Digital Transformation Contract").url("#").source("Press Trust of India").publishedAt(LocalDateTime.now().minusHours(3)).sentimentScore(0.50).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("TCS Announces New AI Platform for Enterprise Clients").url("#").source("LiveMint").publishedAt(LocalDateTime.now().minusHours(7)).sentimentScore(0.55).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Global IT Spending Outlook Positive for TCS in 2024").url("#").source("Financial Express").publishedAt(LocalDateTime.now().minusHours(14)).sentimentScore(0.25).sentimentLabel("SOMEWHAT_BULLISH").build()
        ));
        
        // INFY news
        DEMO_NEWS.put("INFY", Arrays.asList(
            NewsDTO.builder().title("Infosys Launches New Cloud Services Platform").url("#").source("Economic Times").publishedAt(LocalDateTime.now().minusHours(2)).sentimentScore(0.42).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Infosys Q4 Earnings Meet Analyst Expectations").url("#").source("Business Standard").publishedAt(LocalDateTime.now().minusHours(8)).sentimentScore(0.15).sentimentLabel("NEUTRAL").build(),
            NewsDTO.builder().title("European Market Growth Drives Infosys Revenue").url("#").source("Moneycontrol").publishedAt(LocalDateTime.now().minusHours(16)).sentimentScore(0.28).sentimentLabel("SOMEWHAT_BULLISH").build()
        ));
        
        // SUNPHARMA news
        DEMO_NEWS.put("SUNPHARMA", Arrays.asList(
            NewsDTO.builder().title("Sun Pharma's New Drug Gets FDA Approval").url("#").source("Economic Times").publishedAt(LocalDateTime.now().minusHours(1)).sentimentScore(0.60).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("US Market Expansion Plan Announced by Sun Pharma").url("#").source("Business Today").publishedAt(LocalDateTime.now().minusHours(6)).sentimentScore(0.35).sentimentLabel("SOMEWHAT_BULLISH").build(),
            NewsDTO.builder().title("Sun Pharma Reports Strong Q4 Performance").url("#").source("LiveMint").publishedAt(LocalDateTime.now().minusHours(10)).sentimentScore(0.25).sentimentLabel("SOMEWHAT_BULLISH").build()
        ));
        
        // TITAN news
        DEMO_NEWS.put("TITAN", Arrays.asList(
            NewsDTO.builder().title("Titan's Jewellery Sales Surge During Wedding Season").url("#").source("Economic Times").publishedAt(LocalDateTime.now().minusHours(2)).sentimentScore(0.48).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Titan Expands Store Network in Tier-2 Cities").url("#").source("Financial Express").publishedAt(LocalDateTime.now().minusHours(7)).sentimentScore(0.30).sentimentLabel("SOMEWHAT_BULLISH").build(),
            NewsDTO.builder().title("Watch Segment Growth Continues for Titan").url("#").source("Business Standard").publishedAt(LocalDateTime.now().minusHours(13)).sentimentScore(0.22).sentimentLabel("NEUTRAL").build()
        ));
        
        // HINDUNILVR news
        DEMO_NEWS.put("HINDUNILVR", Arrays.asList(
            NewsDTO.builder().title("HUL Launches New Premium skincare Range").url("#").source("LiveMint").publishedAt(LocalDateTime.now().minusHours(3)).sentimentScore(0.35).sentimentLabel("SOMEWHAT_BULLISH").build(),
            NewsDTO.builder().title("Rural Demand Recovery Helps HUL in Q4").url("#").source("Business Today").publishedAt(LocalDateTime.now().minusHours(9)).sentimentScore(0.25).sentimentLabel("NEUTRAL").build(),
            NewsDTO.builder().title("HUL's Digital Commerce Strategy Shows Results").url("#").source("Moneycontrol").publishedAt(LocalDateTime.now().minusHours(15)).sentimentScore(0.20).sentimentLabel("NEUTRAL").build()
        ));
        
        // ICICIBANK news
        DEMO_NEWS.put("ICICIBANK", Arrays.asList(
            NewsDTO.builder().title("ICICI Bank Reports Record Q4 Profit").url("#").source("Economic Times").publishedAt(LocalDateTime.now().minusHours(1)).sentimentScore(0.42).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Digital Banking Leads ICICI Bank's Growth Story").url("#").source("LiveMint").publishedAt(LocalDateTime.now().minusHours(5)).sentimentScore(0.32).sentimentLabel("SOMEWHAT_BULLISH").build(),
            NewsDTO.builder().title("ICICI Bank Maintains Healthy Loan Growth").url("#").source("Business Standard").publishedAt(LocalDateTime.now().minusHours(11)).sentimentScore(0.25).sentimentLabel("SOMEWHAT_BULLISH").build()
        ));
        
        // Default news for other symbols
        DEMO_NEWS.put("DEFAULT", Arrays.asList(
            NewsDTO.builder().title("Stock Shows Mixed Trading Patterns Today").url("#").source("Reuters").publishedAt(LocalDateTime.now().minusHours(2)).sentimentScore(0.10).sentimentLabel("NEUTRAL").build(),
            NewsDTO.builder().title("Sector Performance Remains Cautious").url("#").source("Bloomberg").publishedAt(LocalDateTime.now().minusHours(5)).sentimentScore(0.05).sentimentLabel("NEUTRAL").build(),
            NewsDTO.builder().title("Investors Await Quarterly Results Season").url("#").source("CNBC").publishedAt(LocalDateTime.now().minusHours(8)).sentimentScore(0.15).sentimentLabel("NEUTRAL").build()
        ));
    }

    /**
     * Get real-time news for a specific stock symbol.
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<Map<String, Object>> getNews(@PathVariable String symbol) {
        log.info("Fetching news for symbol: {}", symbol);
        
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // Convert to Alpha Vantage ticker format for news
            String newsTicker = AlphaVantageClient.toNewsTicker(symbol);
            
            // Fetch news from Alpha Vantage
            AlphaVantageClient.NewsSentimentResult result = 
                alphaVantageClient.fetchNewsSentiment(newsTicker);
            
            List<NewsDTO> news = null;
            double avgSentiment = 0;
            boolean isDemo = false;
            
            if (result != null && result.getArticles() != null && !result.getArticles().isEmpty()) {
                news = result.getArticles().stream()
                        .map(this::toNewsDTO)
                        .toList();
                avgSentiment = result.getAvgSentimentScore() != null ? result.getAvgSentimentScore() : 0;
                
                response.put("status", "success");
                response.put("source", "Alpha Vantage");
                response.put("isDemo", false);
            } else {
                // Use demo news when API returns no data
                log.info("No news from API for {}, using demo news", symbol);
                news = DEMO_NEWS.getOrDefault(symbol.toUpperCase(), DEMO_NEWS.get("DEFAULT"));
                avgSentiment = news.stream().mapToDouble(NewsDTO::getSentimentScore).average().orElse(0.15);
                isDemo = true;
                
                response.put("status", "success");
                response.put("source", "Demo Data");
                response.put("isDemo", true);
                response.put("message", "Showing sample news - API limit reached or no data available");
            }
            
            response.put("symbol", symbol);
            response.put("articles", news);
            response.put("count", news.size());
            response.put("avgSentiment", avgSentiment);
            response.put("durationMs", System.currentTimeMillis() - startTime);
            
            log.info("Returning {} news articles for {} (demo={}) in {}ms", 
                    news.size(), symbol, isDemo, System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            log.warn("Error fetching news for {}: {}", symbol, e.getMessage());
            // Return demo news on error
            List<NewsDTO> news = DEMO_NEWS.getOrDefault(symbol.toUpperCase(), DEMO_NEWS.get("DEFAULT"));
            response.put("status", "success");
            response.put("source", "Demo Data (Fallback)");
            response.put("isDemo", true);
            response.put("message", "Showing sample news due to API error");
            response.put("symbol", symbol);
            response.put("articles", news);
            response.put("count", news.size());
            response.put("avgSentiment", news.stream().mapToDouble(NewsDTO::getSentimentScore).average().orElse(0.15));
        }
        
        return ResponseEntity.ok(response);
    }

    private NewsDTO toNewsDTO(AlphaVantageClient.NewsArticle article) {
        return NewsDTO.builder()
                .title(article.getTitle())
                .url(article.getUrl())
                .source(article.getSource())
                .publishedAt(article.getPublishedAt())
                .sentimentScore(article.getSentimentScore())
                .sentimentLabel(article.getSentimentLabel())
                .build();
    }
}

