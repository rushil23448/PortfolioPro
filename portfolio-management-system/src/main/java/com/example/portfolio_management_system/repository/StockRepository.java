package com.example.portfolio_management_system.repository;

import com.example.portfolio_management_system.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, String> {
}
