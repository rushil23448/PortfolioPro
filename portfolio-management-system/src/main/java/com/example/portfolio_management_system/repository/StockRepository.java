package com.example.portfolio_management_system.repository;

import com.example.portfolio_management_system.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, String> {
    List<Stock> findTop5ByOrderByConfidenceScoreDesc();
}
