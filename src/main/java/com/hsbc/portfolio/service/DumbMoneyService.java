package com.hsbc.portfolio.service;

import com.hsbc.portfolio.client.AlphaVantageClient;
import com.hsbc.portfolio.client.YahooFinanceClient;
import com.hsbc.portfolio.dto.DumbMoneyHeatDTO;
import com.hsbc.portfolio.dto.NewsDTO;
import com.hsbc.portfolio.entity.DumbMoneyMetric;
import com.hsbc.portfolio.entity.Stock;
import com.hsbc.portfolio.repository.DumbMoneyMetricRepository;
import com.hsbc.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DumbMoneyService {

    private final DumbMoneyMetricRepository dumbMoneyMetricRepository;
    private final StockRepository stockRepository;
    private final AlphaVantageClient alphaVantageClient;
    private final YahooFinanceClient yahooFinanceClient;
    private final SentimentService sentimentService;

    @Value("${app.demo-mode:false}")
    private boolean demoMode;

    // Demo data for when APIs fail AND DB has no prices
    private static final Map<String, DemoStockData> DEMO_DATA = new HashMap<>();
    static {
        // Major NSE stocks with realistic demo prices
        DEMO_DATA.put("SUNPHARMA", new DemoStockData(1520.50, 2.35, 8540000L));
        DEMO_DATA.put("TITAN", new DemoStockData(3850.75, 1.85, 12500000L));
        DEMO_DATA.put("HDFCBANK", new DemoStockData(1680.25, 0.95, 15200000L));
        DEMO_DATA.put("RELIANCE", new DemoStockData(2950.50, 1.25, 18200000L));
        DEMO_DATA.put("HINDUNILVR", new DemoStockData(2450.00, 0.75, 8900000L));
        DEMO_DATA.put("LT", new DemoStockData(3750.80, 1.55, 6500000L));
        DEMO_DATA.put("TCS", new DemoStockData(4120.30, 2.10, 9800000L));
        DEMO_DATA.put("INFY", new DemoStockData(1520.45, 1.45, 11200000L));
        DEMO_DATA.put("ICICIBANK", new DemoStockData(1020.50, 1.15, 15600000L));
        DEMO_DATA.put("SBIN", new DemoStockData(765.80, 0.65, 18500000L));
        DEMO_DATA.put("BHARTIARTL", new DemoStockData(1420.50, 2.90, 3500000L));
        DEMO_DATA.put("ITC", new DemoStockData(465.75, 1.53, 12000000L));
        DEMO_DATA.put("KOTAKBANK", new DemoStockData(1780.25, 1.43, 2500000L));
        DEMO_DATA.put("AXISBANK", new DemoStockData(1180.50, 2.61, 6000000L));
        DEMO_DATA.put("ASIANPAINT", new DemoStockData(2850.00, 2.52, 800000L));
        DEMO_DATA.put("MARUTI", new DemoStockData(12500.00, 2.46, 450000L));
        DEMO_DATA.put("WIPRO", new DemoStockData(480.25, 3.23, 8500000L));
        DEMO_DATA.put("ULTRACEMCO", new DemoStockData(9850.00, 1.34, 350000L));
        DEMO_DATA.put("BAJFINANCE", new DemoStockData(6850.00, 1.94, 850000L));
        DEMO_DATA.put("NESTLEIND", new DemoStockData(2450.00, 1.24, 120000L));
    }

    public List<DumbMoneyHeatDTO> getHeatMap(LocalDate date) {
        if (date == null) date = LocalDate.now();
        List<DumbMoneyMetric> metrics = dumbMoneyMetricRepository.findByDate(date);
        if (metrics.isEmpty()) {
            log.info("No dumb money metrics found for {}. Calculating fresh...", date);
            metrics = calculateAndSaveDumbMoneyMetrics();
        }
        return metrics.stream()
                .map(this::toDTO)
                .sorted(Comparator.comparing(DumbMoneyHeatDTO::getHeatScore).reversed())
                .collect(Collectors.toList());
    }

    public List<DumbMoneyHeatDTO> getHeatMapToday() {
        return getHeatMap(LocalDate.now());
    }

    public List<DumbMoneyHeatDTO> getHeatMapRealtime() {
        log.info("Fetching real-time dumb money metrics from Yahoo Finance + AI...");
        List<DumbMoneyMetric> metrics = calculateAndSaveDumbMoneyMetrics();
        return metrics.stream()
                .map(this::toDTO)
                .sorted(Comparator.comparing(DumbMoneyHeatDTO::getHeatScore).reversed())
                .collect(Collectors.toList());
    }

    public DumbMoneyHeatDTO getHeatScoreForStock(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol).orElse(null);
        if (stock == null) {
            log.warn("Stock not found: {}", symbol);
            return null;
        }
        
        DumbMoneyMetric metric = calculateMetricForStock(stock, LocalDate.now(), true);
        return toDTO(metric);
    }

    public Map<Long, Double> getHeatScoresByStock() {
        List<DumbMoneyMetric> metrics = dumbMoneyMetricRepository.findByDate(LocalDate.now());
        return metrics.stream()
                .collect(Collectors.toMap(m -> m.getStock().getId(), DumbMoneyMetric::getHeatScore, (a, b) -> b));
    }

    public List<DumbMoneyMetric> calculateAndSaveDumbMoneyMetrics() {
        log.info("Calculating dumb money metrics using real-time data + AI sentiment analysis...");
        
        List<Stock> stocks = stockRepository.findAll();
        LocalDate today = LocalDate.now();
        List<DumbMoneyMetric> metrics = new ArrayList<>();
        
        for (Stock stock : stocks) {
            try {
                DumbMoneyMetric metric = calculateMetricForStock(stock, today, true);
                if (metric != null) {
                    dumbMoneyMetricRepository.save(metric);
                    metrics.add(metric);
                    log.debug("Saved dumb money metric for {}: heat={}, level={}, price={}, vol={}", 
                            stock.getSymbol(), metric.getHeatScore(), metric.getHeatLevel(),
                            metric.getCurrentPrice(), metric.getVolume());
                }
            } catch (Exception e) {
                log.warn("Failed to calculate metric for {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
        log.info("Completed dumb money metric calculation for {} stocks", stocks.size());
        return metrics;
    }

    @Scheduled(fixedRate = 900000) // 15 minutes
    public void scheduledRefresh() {
        log.info("Running scheduled dumb money metrics refresh...");
        calculateAndSaveDumbMoneyMetrics();
    }

    private DumbMoneyMetric calculateMetricForStock(Stock stock, LocalDate date, boolean includeAI) {
        String symbol = stock.getSymbol();
        String exchange = stock.getExchange();
        
        BigDecimal price;
        Long volume;
        Double changePercent;
        boolean apiSuccess = false;
        
        // 1. Try Yahoo Finance first (free, no API key needed)
        String yahooSymbol = YahooFinanceClient.toYahooSymbol(symbol, exchange);
        YahooFinanceClient.QuoteResult yahooQuote = yahooFinanceClient.fetchQuote(yahooSymbol);
        
        if (yahooQuote != null && yahooQuote.getPrice() != null && yahooQuote.getPrice().doubleValue() > 0) {
            price = yahooQuote.getPrice();
            volume = yahooQuote.getVolume();
            changePercent = yahooQuote.getChangePercent();
            apiSuccess = true;
            log.debug("Yahoo Finance data for {}: price={}, vol={}, change={}%", 
                    symbol, price, volume, changePercent);
        } else {
            // 2. Fallback to Alpha Vantage
            log.debug("Yahoo Finance failed for {}, trying Alpha Vantage...", symbol);
            String avSymbol = AlphaVantageClient.toAlphaVantageSymbol(symbol, exchange);
            AlphaVantageClient.QuoteResult avQuote = alphaVantageClient.fetchQuote(avSymbol);
            
            if (avQuote != null && avQuote.getPrice() != null && avQuote.getPrice().doubleValue() > 0) {
                price = avQuote.getPrice();
                volume = avQuote.getVolume();
                changePercent = avQuote.getChangePercent();
                apiSuccess = true;
                log.debug("Alpha Vantage data for {}: price={}, vol={}, change={}%", 
                        symbol, price, volume, changePercent);
            } else {
                // 3. Use demo data if available (for known stocks)
                if (DEMO_DATA.containsKey(symbol)) {
                    DemoStockData demo = DEMO_DATA.get(symbol);
                    price = new BigDecimal(demo.price);
                    volume = demo.volume;
                    changePercent = demo.changePercent;
                    apiSuccess = false;
                    log.debug("Using demo data for {}: price={}, vol={}, change={}%", 
                            symbol, price, volume, changePercent);
                } else {
                    // 4. Fallback to database values
                    price = stock.getCurrentPrice();
                    volume = stock.getVolume();
                    changePercent = stock.getChangePercent();
                    
                    // Generate reasonable fallback values if DB is also empty
                    if (price == null || price.doubleValue() <= 0) {
                        price = new BigDecimal(1000.0);
                        changePercent = 0.0;
                    }
                    if (volume == null || volume <= 0) {
                        volume = 5000000L;
                    }
                    apiSuccess = false;
                    log.debug("Using DB/default values for {}: price={}, vol={}", 
                            symbol, price, volume);
                }
            }
        }
        
        // 5. Get news headlines from Alpha Vantage for AI sentiment
        SentimentService.SentimentAnalysis aiAnalysis;
        if (includeAI) {
            String newsTicker = AlphaVantageClient.toNewsTicker(yahooSymbol != null ? yahooSymbol : symbol);
            AlphaVantageClient.NewsSentimentResult newsResult = alphaVantageClient.fetchNewsSentiment(newsTicker);
            
            if (newsResult != null && newsResult.getHeadlines() != null && !newsResult.getHeadlines().isEmpty()) {
                aiAnalysis = sentimentService.analyzeHeadlines(symbol, newsResult.getHeadlines());
            } else {
                log.debug("No news found for {}. Using default sentiment.", symbol);
                aiAnalysis = SentimentService.SentimentAnalysis.builder()
                        .symbol(symbol)
                        .score(50.0)
                        .classification("NEUTRAL")
                        .reasoning("No news available - using default neutral sentiment")
                        .build();
            }
        } else {
            aiAnalysis = SentimentService.SentimentAnalysis.builder()
                    .symbol(symbol)
                    .score(50.0)
                    .classification("NEUTRAL")
                    .reasoning("AI analysis skipped")
                    .build();
        }
        
        // 6. Calculate normalized scores (0-100)
        double priceScore = normalizePrice(price);
        double volumeScore = normalizeVolume(volume, stock);
        double aiSentimentScore = aiAnalysis.getScore();
        
        // 7. Calculate combined dumb money score
        double heatScore = (priceScore * 0.30) + (volumeScore * 0.30) + (aiSentimentScore * 0.40);
        heatScore = Math.round(heatScore * 100) / 100.0;
        
        // 8. Determine heat level
        String heatLevel = determineHeatLevel(heatScore);
        
        // 9. Determine trend based on change percent
        String trend = determineTrend(changePercent);
        double trendStrength = calculateTrendStrength(changePercent);
        
        // 10. Market cap category
        String marketCapCategory = determineMarketCapCategory(price, volume);
        
        // 11. Retail flow score
        double retailFlowScore = Math.round(((volumeScore + aiSentimentScore) / 2) * 100) / 100.0;
        
        // 12. Buzz score
        double buzzScore = aiSentimentScore;
        
        return DumbMoneyMetric.builder()
                .stock(stock)
                .date(date)
                .currentPrice(price != null ? price.doubleValue() : 0.0)
                .changePercent(changePercent != null ? changePercent : 0.0)
                .volume(volume != null ? volume : 0L)
                .priceScore(priceScore)
                .volumeScore(volumeScore)
                .aiSentimentScore(aiSentimentScore)
                .retailFlowScore(retailFlowScore)
                .buzzScore(buzzScore)
                .heatScore(heatScore)
                .heatLevel(heatLevel)
                .trend(trend)
                .trendStrength(trendStrength)
                .marketCapCategory(marketCapCategory)
                .aiReasoning(aiAnalysis.getReasoning())
                .sentimentClassification(aiAnalysis.getClassification())
                .build();
    }

    private double normalizePrice(BigDecimal price) {
        if (price == null) return 50.0;
        double priceValue = price.doubleValue();
        
        if (priceValue <= 100) return 20;
        if (priceValue <= 500) return 35;
        if (priceValue <= 1000) return 45;
        if (priceValue <= 2000) return 55;
        if (priceValue <= 5000) return 70;
        if (priceValue <= 10000) return 80;
        if (priceValue <= 20000) return 90;
        return 95;
    }

    private double normalizeVolume(Long volume, Stock stock) {
        if (volume == null || volume <= 0) return 30.0;
        double vol = volume.doubleValue();
        
        if (vol < 100000) return 15;
        if (vol < 500000) return 30;
        if (vol < 1000000) return 45;
        if (vol < 5000000) return 60;
        if (vol < 10000000) return 75;
        if (vol < 50000000) return 85;
        return 95;
    }

    private String determineHeatLevel(double score) {
        if (score >= 75) return "OVERHEATED";
        if (score >= 55) return "WARM";
        if (score >= 35) return "NEUTRAL";
        return "COOL";
    }

    private String determineTrend(Double changePercent) {
        if (changePercent == null) return "STABLE";
        if (changePercent >= 3.0) return "STRONG_UP";
        if (changePercent >= 1.0) return "UP";
        if (changePercent >= -1.0) return "STABLE";
        if (changePercent >= -3.0) return "DOWN";
        return "STRONG_DOWN";
    }

    private double calculateTrendStrength(Double changePercent) {
        if (changePercent == null) return 50.0;
        double absChange = Math.abs(changePercent);
        if (absChange >= 10) return 95;
        if (absChange >= 5) return 80;
        if (absChange >= 3) return 65;
        if (absChange >= 1) return 50;
        if (absChange >= 0.5) return 35;
        return 20;
    }

    private String determineMarketCapCategory(BigDecimal price, Long volume) {
        if (price == null || volume == null) return "UNKNOWN";
        double marketCap = price.doubleValue() * volume.doubleValue();
        if (marketCap > 10000000000000.0) return "LARGE";
        if (marketCap > 1000000000000.0) return "MID";
        if (marketCap > 100000000000.0) return "SMALL";
        return "MICRO";
    }

    private DumbMoneyHeatDTO toDTO(DumbMoneyMetric m) {
        return toDTOWithNews(m, null);
    }
    
    private DumbMoneyHeatDTO toDTOWithNews(DumbMoneyMetric m, List<NewsDTO> newsHeadlines) {
        Stock s = m.getStock();
        return DumbMoneyHeatDTO.builder()
                .stockId(s.getId())
                .symbol(s.getSymbol())
                .name(s.getName())
                .sector(s.getSector())
                .exchange(s.getExchange())
                .currentPrice(m.getCurrentPrice())
                .changePercent(m.getChangePercent())
                .volume(m.getVolume())
                .priceScore(m.getPriceScore())
                .aiSentimentScore(m.getAiSentimentScore())
                .retailFlowScore(m.getRetailFlowScore())
                .buzzScore(m.getBuzzScore())
                .heatScore(m.getHeatScore())
                .heatLevel(m.getHeatLevel())
                .aiReasoning(m.getAiReasoning())
                .sentimentClassification(m.getSentimentClassification())
                .newsHeadlines(newsHeadlines)
                .trend(m.getTrend())
                .trendStrength(m.getTrendStrength())
                .marketCapCategory(m.getMarketCapCategory())
                .lastUpdated(LocalDateTime.now())
                .build();
    }
    
    /**
     * Get heat map with fresh news headlines included.
     */
    public List<DumbMoneyHeatDTO> getHeatMapWithNews() {
        log.info("Fetching real-time dumb money metrics with fresh news...");
        List<Stock> stocks = stockRepository.findAll();
        List<DumbMoneyHeatDTO> results = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (Stock stock : stocks) {
            try {
                DumbMoneyMetric metric = calculateMetricForStock(stock, today, true);
                if (metric != null) {
                    // Fetch fresh news for this stock
                    String newsTicker = AlphaVantageClient.toNewsTicker(stock.getSymbol());
                    AlphaVantageClient.NewsSentimentResult newsResult = 
                        alphaVantageClient.fetchNewsSentiment(newsTicker);
                    
                    List<NewsDTO> newsHeadlines = null;
                    if (newsResult != null && newsResult.getArticles() != null && !newsResult.getArticles().isEmpty()) {
                        // Use real news from Alpha Vantage
                        newsHeadlines = newsResult.getArticles().stream()
                                .map(a -> NewsDTO.builder()
                                        .title(a.getTitle())
                                        .url(a.getUrl())
                                        .source(a.getSource())
                                        .publishedAt(a.getPublishedAt())
                                        .sentimentScore(a.getSentimentScore())
                                        .sentimentLabel(a.getSentimentLabel())
                                        .build())
                                .collect(Collectors.toList());
                        log.debug("Got {} real news articles for {}", newsHeadlines.size(), stock.getSymbol());
                    } else {
                        // Use demo news when API returns no data
                        log.debug("No news from API for {}, using demo news", stock.getSymbol());
                        newsHeadlines = getDemoNews(stock.getSymbol());
                    }
                    
                    DumbMoneyHeatDTO dto = toDTOWithNews(metric, newsHeadlines);
                    results.add(dto);
                }
            } catch (Exception e) {
                log.warn("Failed to calculate metric for {}: {}", stock.getSymbol(), e.getMessage());
                // Try to get demo news as fallback
                List<NewsDTO> demoNews = getDemoNews(stock.getSymbol());
                DumbMoneyMetric fallbackMetric = calculateMetricForStock(stock, today, true);
                if (fallbackMetric != null) {
                    DumbMoneyHeatDTO dto = toDTOWithNews(fallbackMetric, demoNews);
                    results.add(dto);
                }
            }
        }
        
        return results.stream()
                .sorted(Comparator.comparing(DumbMoneyHeatDTO::getHeatScore).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Get demo news for a stock symbol when the real API is unavailable.
     */
    private List<NewsDTO> getDemoNews(String symbol) {
        String upperSymbol = symbol.toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        
        Map<String, List<NewsDTO>> demoNewsMap = new HashMap<>();
        demoNewsMap.put("RELIANCE", Arrays.asList(
            NewsDTO.builder().title("Reliance Industries Q4 Results Beat Street Estimates").url("#").source("ET").publishedAt(now.minusHours(2)).sentimentScore(0.45).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Reliance Jio Adds 5 Million New Subscribers").url("#").source("LiveMint").publishedAt(now.minusHours(6)).sentimentScore(0.35).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Retail Business Drives Growth for Reliance in FY24").url("#").source("Business Standard").publishedAt(now.minusHours(12)).sentimentScore(0.25).sentimentLabel("SOMEWHAT_BULLISH").build()
        ));
        demoNewsMap.put("HDFCBANK", Arrays.asList(
            NewsDTO.builder().title("HDFC Bank Reports Strong Q4 Profit Growth").url("#").source("Economic Times").publishedAt(now.minusHours(1)).sentimentScore(0.40).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Digital Banking Initiatives Boost HDFC Bank's Customer Base").url("#").source("Moneycontrol").publishedAt(now.minusHours(5)).sentimentScore(0.30).sentimentLabel("SOMEWHAT_BULLISH").build(),
            NewsDTO.builder().title("HDFC Bank Maintains Steady Asset Quality in Q4").url("#").source("Business Today").publishedAt(now.minusHours(8)).sentimentScore(0.20).sentimentLabel("NEUTRAL").build()
        ));
        demoNewsMap.put("TCS", Arrays.asList(
            NewsDTO.builder().title("TCS Wins Multi-Year Digital Transformation Contract").url("#").source("Press Trust of India").publishedAt(now.minusHours(3)).sentimentScore(0.50).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("TCS Announces New AI Platform for Enterprise Clients").url("#").source("LiveMint").publishedAt(now.minusHours(7)).sentimentScore(0.55).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Global IT Spending Outlook Positive for TCS in 2024").url("#").source("Financial Express").publishedAt(now.minusHours(14)).sentimentScore(0.25).sentimentLabel("SOMEWHAT_BULLISH").build()
        ));
        demoNewsMap.put("INFY", Arrays.asList(
            NewsDTO.builder().title("Infosys Launches New Cloud Services Platform").url("#").source("Economic Times").publishedAt(now.minusHours(2)).sentimentScore(0.42).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Infosys Q4 Earnings Meet Analyst Expectations").url("#").source("Business Standard").publishedAt(now.minusHours(8)).sentimentScore(0.15).sentimentLabel("NEUTRAL").build(),
            NewsDTO.builder().title("European Market Growth Drives Infosys Revenue").url("#").source("Moneycontrol").publishedAt(now.minusHours(16)).sentimentScore(0.28).sentimentLabel("SOMEWHAT_BULLISH").build()
        ));
        demoNewsMap.put("SUNPHARMA", Arrays.asList(
            NewsDTO.builder().title("Sun Pharma's New Drug Gets FDA Approval").url("#").source("Economic Times").publishedAt(now.minusHours(1)).sentimentScore(0.60).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("US Market Expansion Plan Announced by Sun Pharma").url("#").source("Business Today").publishedAt(now.minusHours(6)).sentimentScore(0.35).sentimentLabel("SOMEWHAT_BULLISH").build(),
            NewsDTO.builder().title("Sun Pharma Reports Strong Q4 Performance").url("#").source("LiveMint").publishedAt(now.minusHours(10)).sentimentScore(0.25).sentimentLabel("SOMEWHAT_BULLISH").build()
        ));
        demoNewsMap.put("TITAN", Arrays.asList(
            NewsDTO.builder().title("Titan's Jewellery Sales Surge During Wedding Season").url("#").source("Economic Times").publishedAt(now.minusHours(2)).sentimentScore(0.48).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Titan Expands Store Network in Tier-2 Cities").url("#").source("Financial Express").publishedAt(now.minusHours(7)).sentimentScore(0.30).sentimentLabel("SOMEWHAT_BULLISH").build(),
            NewsDTO.builder().title("Watch Segment Growth Continues for Titan").url("#").source("Business Standard").publishedAt(now.minusHours(13)).sentimentScore(0.22).sentimentLabel("NEUTRAL").build()
        ));
        demoNewsMap.put("HINDUNILVR", Arrays.asList(
            NewsDTO.builder().title("HUL Launches New Premium Skincare Range").url("#").source("LiveMint").publishedAt(now.minusHours(3)).sentimentScore(0.35).sentimentLabel("SOMEWHAT_BULLISH").build(),
            NewsDTO.builder().title("Rural Demand Recovery Helps HUL in Q4").url("#").source("Business Today").publishedAt(now.minusHours(9)).sentimentScore(0.25).sentimentLabel("NEUTRAL").build(),
            NewsDTO.builder().title("HUL's Digital Commerce Strategy Shows Results").url("#").source("Moneycontrol").publishedAt(now.minusHours(15)).sentimentScore(0.20).sentimentLabel("NEUTRAL").build()
        ));
        demoNewsMap.put("ICICIBANK", Arrays.asList(
            NewsDTO.builder().title("ICICI Bank Reports Record Q4 Profit").url("#").source("Economic Times").publishedAt(now.minusHours(1)).sentimentScore(0.42).sentimentLabel("BULLISH").build(),
            NewsDTO.builder().title("Digital Banking Leads ICICI Bank's Growth Story").url("#").source("LiveMint").publishedAt(now.minusHours(5)).sentimentScore(0.32).sentimentLabel("SOMEWHAT_BULLISH").build(),
            NewsDTO.builder().title("ICICI Bank Maintains Healthy Loan Growth").url("#").source("Business Standard").publishedAt(now.minusHours(11)).sentimentScore(0.25).sentimentLabel("SOMEWHAT_BULLISH").build()
        ));
        
        // Default news for unknown symbols
        List<NewsDTO> defaultNews = Arrays.asList(
            NewsDTO.builder().title("Stock Shows Mixed Trading Patterns Today").url("#").source("Reuters").publishedAt(now.minusHours(2)).sentimentScore(0.10).sentimentLabel("NEUTRAL").build(),
            NewsDTO.builder().title("Sector Performance Remains Cautious").url("#").source("Bloomberg").publishedAt(now.minusHours(5)).sentimentScore(0.05).sentimentLabel("NEUTRAL").build(),
            NewsDTO.builder().title("Investors Await Quarterly Results Season").url("#").source("CNBC").publishedAt(now.minusHours(8)).sentimentScore(0.15).sentimentLabel("NEUTRAL").build()
        );
        
        return demoNewsMap.getOrDefault(upperSymbol, defaultNews);
    }

    private static class DemoStockData {
        final double price;
        final double changePercent;
        final long volume;
        
        DemoStockData(double price, double changePercent, long volume) {
            this.price = price;
            this.changePercent = changePercent;
            this.volume = volume;
        }
    }
}

