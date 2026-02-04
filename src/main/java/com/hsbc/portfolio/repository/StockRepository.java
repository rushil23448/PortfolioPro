package com.hsbc.portfolio.repository;

import com.hsbc.portfolio.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);
    List<Stock> findByExchange(String exchange);
    List<Stock> findBySector(String sector);
}
