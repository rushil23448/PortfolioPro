package com.hsbc.portfolio.service;

import com.hsbc.portfolio.dto.StockDTO;
import com.hsbc.portfolio.entity.Stock;
import com.hsbc.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    public List<StockDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(StockService::toDTOStatic)
                .collect(Collectors.toList());
    }

    public List<StockDTO> getStocksByExchange(String exchange) {
        return stockRepository.findByExchange(exchange).stream()
                .map(StockService::toDTOStatic)
                .collect(Collectors.toList());
    }

    public StockDTO getBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol)
                .map(StockService::toDTOStatic)
                .orElse(null);
    }

    public Stock getEntityById(Long id) {
        return stockRepository.findById(id).orElse(null);
    }

    public Stock getEntityBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol).orElse(null);
    }

    public static StockDTO toDTOStatic(Stock s) {
        return StockDTO.builder()
                .id(s.getId())
                .symbol(s.getSymbol())
                .name(s.getName())
                .sector(s.getSector())
                .exchange(s.getExchange())
                .currentPrice(s.getCurrentPrice())
                .previousClose(s.getPreviousClose())
                .changePercent(s.getChangePercent())
                .volume(s.getVolume())
                .peRatio(s.getPeRatio())
                .marketCap(s.getMarketCap())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
