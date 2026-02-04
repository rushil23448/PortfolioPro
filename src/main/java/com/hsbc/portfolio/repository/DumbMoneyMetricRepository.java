package com.hsbc.portfolio.repository;

import com.hsbc.portfolio.entity.DumbMoneyMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DumbMoneyMetricRepository extends JpaRepository<DumbMoneyMetric, Long> {

    List<DumbMoneyMetric> findByDate(LocalDate date);
    List<DumbMoneyMetric> findByStockIdAndDateBetween(Long stockId, LocalDate start, LocalDate end);
}
