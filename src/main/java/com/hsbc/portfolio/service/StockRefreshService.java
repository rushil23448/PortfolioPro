package com.hsbc.portfolio.service;

import com.hsbc.portfolio.client.AlphaVantageClient;
import com.hsbc.portfolio.entity.Stock;
import com.hsbc.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Refreshes stock prices from Alpha Vantage.
 * Free tier allows ~25 requests/day; we add delay between calls to reduce rate-limit risk (5/min typical).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockRefreshService {

    private final StockRepository stockRepository;
    private final AlphaVantageClient alphaVantageClient;
    private final com.hsbc.portfolio.config.AlphaVantageConfig alphaVantageConfig;

    /**
     * Refresh prices for given symbols (or all stocks if empty).
     * Delay between requests to respect free-tier limits.
     */
    public RefreshResult refreshPrices(List<String> symbols) {
        if (!alphaVantageConfig.isConfigured()) {
            return RefreshResult.builder()
                    .updated(0)
                    .failed(0)
                    .message("Alpha Vantage API key not set. Set alpha.vantage.api-key in application.properties or env ALPHA_VANTAGE_API_KEY.")
                    .build();
        }
        List<Stock> toRefresh;
        if (symbols == null || symbols.isEmpty()) {
            toRefresh = stockRepository.findAll();
        } else {
            Map<String, Stock> bySymbol = new LinkedHashMap<>();
            for (String s : symbols) {
                String sym = s.trim();
                if (sym.isEmpty()) continue;
                stockRepository.findBySymbol(sym).ifPresent(stock -> bySymbol.putIfAbsent(stock.getSymbol(), stock));
            }
            toRefresh = new ArrayList<>(bySymbol.values());
        }
        int updated = 0;
        int failed = 0;
        for (Stock stock : toRefresh) {
            String avSymbol = AlphaVantageClient.toAlphaVantageSymbol(stock.getSymbol(), stock.getExchange());
            AlphaVantageClient.QuoteResult quote = alphaVantageClient.fetchQuote(avSymbol);
            if (quote != null && quote.getPrice() != null) {
                stock.setCurrentPrice(quote.getPrice());
                if (quote.getPreviousClose() != null) stock.setPreviousClose(quote.getPreviousClose());
                if (quote.getChangePercent() != null) stock.setChangePercent(quote.getChangePercent());
                if (quote.getVolume() != null) stock.setVolume(quote.getVolume());
                stock.setUpdatedAt(LocalDateTime.now());
                stockRepository.save(stock);
                updated++;
            } else {
                failed++;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return RefreshResult.builder()
                .updated(updated)
                .failed(failed)
                .message("Refreshed " + updated + " stocks. Failed: " + failed + ". Free tier: use refresh sparingly.")
                .build();
    }

    @lombok.Builder
    @lombok.Getter
    public static class RefreshResult {
        private final int updated;
        private final int failed;
        private final String message;
    }
}
