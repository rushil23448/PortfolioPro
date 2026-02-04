package com.hsbc.portfolio.controller;

import com.hsbc.portfolio.dto.StockDTO;
import com.hsbc.portfolio.entity.Stock;
import com.hsbc.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for fetching stock data.
 */
@Slf4j
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository;

    @GetMapping
    public ResponseEntity<List<StockDTO>> getAllStocks(
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String sector) {
        
        log.info("Fetching stocks - exchange: {}, sector: {}", exchange, sector);
        
        List<Stock> stocks;
        
        if (exchange != null && !exchange.isBlank()) {
            stocks = stockRepository.findByExchange(exchange.toUpperCase());
        } else if (sector != null && !sector.isBlank()) {
            stocks = stockRepository.findAll().stream()
                    .filter(s -> sector.equalsIgnoreCase(s.getSector()))
                    .collect(Collectors.toList());
        } else {
            stocks = stockRepository.findAll();
        }
        
        List<StockDTO> dtos = stocks.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        log.info("Returning {} stocks", dtos.size());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<StockDTO> getStockBySymbol(@PathVariable String symbol) {
        log.info("Fetching stock by symbol: {}", symbol);
        
        return stockRepository.findBySymbol(symbol.toUpperCase())
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<StockDTO>> searchStocks(@RequestParam String query) {
        log.info("Searching stocks with query: {}", query);
        
        List<Stock> stocks = stockRepository.findAll().stream()
                .filter(s -> s.getSymbol().toUpperCase().contains(query.toUpperCase()) ||
                             (s.getName() != null && s.getName().toUpperCase().contains(query.toUpperCase())))
                .limit(10)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(stocks.stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
    }

    private StockDTO toDTO(Stock stock) {
        Double marketCapValue = null;
        if (stock.getMarketCap() != null) {
            marketCapValue = stock.getMarketCap().doubleValue();
        }
        
        BigDecimal currentPriceValue = stock.getCurrentPrice() != null 
                ? stock.getCurrentPrice() 
                : BigDecimal.ZERO;
        
        return StockDTO.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .sector(stock.getSector())
                .exchange(stock.getExchange())
                .currentPrice(currentPriceValue)
                .changePercent(stock.getChangePercent())
                .volume(stock.getVolume())
                .peRatio(stock.getPeRatio())
                .marketCap(marketCapValue)
                .build();
    }
}

