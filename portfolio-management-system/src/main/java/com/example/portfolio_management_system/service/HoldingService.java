package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.HolderRepository;
import com.example.portfolio_management_system.repository.HoldingRepository;
import com.example.portfolio_management_system.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final HolderRepository holderRepository;
    private final StockRepository stockRepository;

    public Holding addHolding(Long holderId, String stockSymbol, Integer quantity, Double price) {

        Holder holder = holderRepository.findById(holderId)
                .orElseThrow(() -> new RuntimeException("Holder not found with ID: " + holderId));

        // ✅ Uses the String symbol to find the stock
        Stock stock = stockRepository.findById(stockSymbol)
                .orElseThrow(() -> new RuntimeException("Stock not found with Symbol: " + stockSymbol));

        Holding holding = new Holding();
        holding.setHolder(holder);
        holding.setStock(stock);
        holding.setQuantity(quantity);
        holding.setAvgPrice(price);

        return holdingRepository.save(holding);
    }

    public List<Holding> getHoldingsByHolder(Long holderId) {
        return holdingRepository.findByHolderId(holderId);
    }
}