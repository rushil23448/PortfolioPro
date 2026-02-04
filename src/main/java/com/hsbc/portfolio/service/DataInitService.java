package com.hsbc.portfolio.service;

import com.hsbc.portfolio.entity.DumbMoneyMetric;
import com.hsbc.portfolio.entity.Stock;
import com.hsbc.portfolio.repository.DumbMoneyMetricRepository;
import com.hsbc.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Seeds sample BSE/NSE stocks and dumb money heat data for demo.
 */
@Service
@RequiredArgsConstructor
public class DataInitService {

    private final StockRepository stockRepository;
    private final DumbMoneyMetricRepository dumbMoneyMetricRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        if (stockRepository.count() > 0) return;

        List<Stock> stocks = List.of(
                createStock("RELIANCE", "Reliance Industries", "Energy", "NSE", 2450, 2420, 1.24, 5000000L, 28.5, 1650000),
                createStock("TCS", "Tata Consultancy Services", "IT", "NSE", 3850, 3820, 0.79, 1200000L, 32.0, 1400000),
                createStock("HDFCBANK", "HDFC Bank", "Banking", "NSE", 1680, 1650, 1.82, 8000000L, 19.0, 1100000),
                createStock("INFY", "Infosys", "IT", "NSE", 1520, 1480, 2.70, 4500000L, 25.0, 630000),
                createStock("ICICIBANK", "ICICI Bank", "Banking", "NSE", 1120, 1090, 2.75, 9000000L, 18.5, 780000),
                createStock("HINDUNILVR", "Hindustan Unilever", "FMCG", "NSE", 2450, 2480, -1.21, 1500000L, 55.0, 580000),
                createStock("SBIN", "State Bank of India", "Banking", "NSE", 780, 755, 3.31, 25000000L, 12.0, 700000),
                createStock("BHARTIARTL", "Bharti Airtel", "Telecom", "NSE", 1420, 1380, 2.90, 3500000L, 45.0, 770000),
                createStock("ITC", "ITC Ltd", "FMCG", "NSE", 465, 458, 1.53, 12000000L, 22.0, 580000),
                createStock("KOTAKBANK", "Kotak Mahindra Bank", "Banking", "NSE", 1780, 1755, 1.43, 2500000L, 16.0, 350000),
                createStock("LT", "Larsen & Toubro", "Infrastructure", "NSE", 3650, 3580, 1.96, 1800000L, 28.0, 510000),
                createStock("AXISBANK", "Axis Bank", "Banking", "NSE", 1180, 1150, 2.61, 6000000L, 14.0, 360000),
                createStock("ASIANPAINT", "Asian Paints", "Paints", "NSE", 2850, 2780, 2.52, 800000L, 48.0, 270000),
                createStock("MARUTI", "Maruti Suzuki", "Auto", "NSE", 12500, 12200, 2.46, 450000L, 22.0, 380000),
                createStock("TITAN", "Titan Company", "Consumer", "NSE", 3450, 3380, 2.07, 600000L, 65.0, 305000),
                createStock("WIPRO", "Wipro Ltd", "IT", "NSE", 480, 465, 3.23, 8500000L, 18.0, 260000),
                createStock("SUNPHARMA", "Sun Pharma", "Pharma", "NSE", 1420, 1395, 1.79, 3200000L, 35.0, 340000),
                createStock("ULTRACEMCO", "UltraTech Cement", "Cement", "NSE", 9850, 9720, 1.34, 350000L, 28.0, 285000),
                createStock("BAJFINANCE", "Bajaj Finance", "NBFC", "NSE", 6850, 6720, 1.94, 850000L, 28.0, 400000),
                createStock("NESTLEIND", "Nestle India", "FMCG", "NSE", 2450, 2420, 1.24, 120000L, 65.0, 236000)
        );
        stockRepository.saveAll(stocks);

        Random rand = new Random(42);
        LocalDate today = LocalDate.now();
        for (Stock s : stocks) {
            double retail = 30 + rand.nextDouble() * 60;
            double buzz = 25 + rand.nextDouble() * 55;
            double heat = (retail + buzz) / 2;
            String level = heat >= 70 ? "OVERHEATED" : heat >= 50 ? "WARM" : heat >= 30 ? "NEUTRAL" : "COOL";
            DumbMoneyMetric m = DumbMoneyMetric.builder()
                    .stock(s)
                    .date(today)
                    .retailFlowScore(Math.round(retail * 100) / 100.0)
                    .buzzScore(Math.round(buzz * 100) / 100.0)
                    .heatScore(Math.round(heat * 100) / 100.0)
                    .heatLevel(level)
                    .build();
            dumbMoneyMetricRepository.save(m);
        }
    }

    private Stock createStock(String symbol, String name, String sector, String exchange,
                              double price, double prev, double chgPct, long vol, double pe, double mcap) {
        return Stock.builder()
                .symbol(symbol)
                .name(name)
                .sector(sector)
                .exchange(exchange)
                .currentPrice(BigDecimal.valueOf(price))
                .previousClose(BigDecimal.valueOf(prev))
                .changePercent(chgPct)
                .volume(vol)
                .peRatio(pe)
                .marketCap(mcap)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
