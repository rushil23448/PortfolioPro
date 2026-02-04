package com.hsbc.portfolio.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Yahoo Finance API client for real-time stock prices.
 * FREE - No API key required, unlimited requests.
 * 
 * For Indian stocks:
 * - NSE: Use "NSE:SYMBOL" (e.g., "NSE:RELIANCE")
 * - BSE: Use "BSE:SYMBOL" (e.g., "BSE:500325")
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YahooFinanceClient {

    // Yahoo Finance quote endpoint (modern API)
    private static final String QUOTE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&range=1d";
    
    // Alternative: Using finance.yahoo.com quote page scraping
    private static final String SCRAPE_URL = "https://finance.yahoo.com/quote/%s";

    // Use the configured RestTemplate with proper headers
    private final RestTemplate yahooFinanceRestTemplate;
    
    // Fallback RestTemplate in case injection fails
    private RestTemplate fallbackRestTemplate;

    /**
     * Fetch real-time quote for any symbol.
     * For Indian stocks: use "NSE:RELIANCE" or "BSE:500325"
     */
    public QuoteResult fetchQuote(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            log.warn("Yahoo Finance: Invalid symbol");
            return null;
        }

        // Try up to 3 times with exponential backoff
        int maxRetries = 3;
        long initialDelay = 1000; // 1 second
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                // Try modern API first
                String url = String.format(QUOTE_URL, symbol);
                Map<String, Object> response = getRestTemplate().getForObject(url, Map.class);
                
                if (response != null && response.containsKey("chart")) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) 
                        ((Map<String, Object>) response.get("chart")).get("result");
                    if (results != null && !results.isEmpty()) {
                        return parseYahooResult(results.get(0));
                    }
                }
                
                // Fallback: Try scraping approach
                return fetchQuoteByScrape(symbol);
                
            } catch (Exception e) {
                log.debug("Yahoo Finance API attempt {}/{} failed for {}: {}", 
                    attempt + 1, maxRetries, symbol, e.getMessage());
                
                if (attempt < maxRetries - 1) {
                    try {
                        long delay = initialDelay * (long) Math.pow(2, attempt);
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        log.warn("Yahoo Finance failed for {} after {} attempts", symbol, maxRetries);
        return null;
    }

    private RestTemplate getRestTemplate() {
        if (yahooFinanceRestTemplate != null) {
            return yahooFinanceRestTemplate;
        }
        if (fallbackRestTemplate == null) {
            fallbackRestTemplate = new RestTemplate();
        }
        return fallbackRestTemplate;
    }

    /**
     * Parse Yahoo Finance chart API response
     */
    @SuppressWarnings("unchecked")
    private QuoteResult parseYahooResult(Map<String, Object> result) {
        try {
            Map<String, Object> meta = (Map<String, Object>) result.get("meta");
            if (meta == null) return null;

            Object priceObj = meta.get("regularMarketPrice");
            Object prevCloseObj = meta.get("previousClose");
            Object volumeObj = meta.get("regularMarketVolume");
            Object changeObj = meta.get("regularMarketChange");

            BigDecimal price = parseDecimal(priceObj);
            BigDecimal previousClose = parseDecimal(prevCloseObj);
            Double changePercent = null;
            
            if (changeObj != null && price != null && previousClose != null && previousClose.doubleValue() > 0) {
                changePercent = ((price.doubleValue() - previousClose.doubleValue()) / previousClose.doubleValue()) * 100;
            }

            Long volume = null;
            if (volumeObj != null) {
                try {
                    volume = Long.parseLong(volumeObj.toString());
                } catch (NumberFormatException ignored) {}
            }

            return QuoteResult.builder()
                    .price(price)
                    .previousClose(previousClose)
                    .changePercent(changePercent != null ? Math.round(changePercent * 100) / 100.0 : 0.0)
                    .volume(volume)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse Yahoo Finance result: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Fallback: Parse Yahoo Finance quote page for price data
     * This is less reliable but works when API fails
     */
    private QuoteResult fetchQuoteByScrape(String symbol) {
        try {
            String url = String.format(SCRAPE_URL, symbol);
            String html = getRestTemplate().getForObject(url, String.class);
            
            if (html == null || html.isBlank()) return null;

            // Extract price using regex patterns
            java.util.regex.Pattern pricePattern = java.util.regex.Pattern.compile("\"regularMarketPrice\":\\s*([\\d.]+)");
            java.util.regex.Pattern changePercentPattern = java.util.regex.Pattern.compile("\"regularMarketChangePercent\":\\s*([\\d.-]+)");
            java.util.regex.Pattern volumePattern = java.util.regex.Pattern.compile("\"regularMarketVolume\":\\s*(\\d+)");

            java.util.regex.Matcher priceMatcher = pricePattern.matcher(html);
            if (priceMatcher.find()) {
                BigDecimal price = new BigDecimal(priceMatcher.group(1));
                
                Double changePercent = 0.0;
                java.util.regex.Matcher changePercentMatcher = changePercentPattern.matcher(html);
                if (changePercentMatcher.find()) {
                    try {
                        changePercent = Double.parseDouble(changePercentMatcher.group(1));
                    } catch (NumberFormatException ignored) {}
                }

                Long volume = null;
                java.util.regex.Matcher volumeMatcher = volumePattern.matcher(html);
                if (volumeMatcher.find()) {
                    try {
                        volume = Long.parseLong(volumeMatcher.group(1));
                    } catch (NumberFormatException ignored) {}
                }

                return QuoteResult.builder()
                        .price(price)
                        .previousClose(price) // Yahoo API doesn't give prev close in scrape
                        .changePercent(changePercent)
                        .volume(volume)
                        .build();
            }
            
            return null;
        } catch (Exception e) {
            log.debug("Scrape fallback failed for {}: {}", symbol, e.getMessage());
            return null;
        }
    }

    private BigDecimal parseDecimal(Object obj) {
        if (obj == null) return null;
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert our stock symbol + exchange to Yahoo Finance format
     * NSE: RELIANCE -> NSE:RELIANCE
     * BSE: 500325 -> BSE:500325
     */
    public static String toYahooSymbol(String symbol, String exchange) {
        if (symbol == null || symbol.isBlank()) return null;
        if ("NSE".equalsIgnoreCase(exchange)) return "NSE:" + symbol.toUpperCase();
        if ("BSE".equalsIgnoreCase(exchange)) return "BSE:" + symbol.toUpperCase();
        return symbol.toUpperCase();
    }

    @lombok.Builder
    @lombok.Getter
    public static class QuoteResult {
        private final BigDecimal price;
        private final BigDecimal previousClose;
        private final Double changePercent;
        private final Long volume;
    }
}

