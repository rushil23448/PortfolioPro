package com.hsbc.portfolio.controller;

import com.hsbc.portfolio.dto.PortfolioDiversificationDTO;
import com.hsbc.portfolio.entity.Portfolio;
import com.hsbc.portfolio.entity.PortfolioHolding;
import com.hsbc.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    private static final String DEFAULT_USER = "user1";

    @GetMapping("/diversification")
    public ResponseEntity<PortfolioDiversificationDTO> getDiversification(
            @RequestParam(required = false) Long portfolioId) {
        Long pid = portfolioId;
        if (pid == null) {
            Portfolio p = portfolioService.getOrCreateDefault(DEFAULT_USER);
            pid = p.getId();
        }
        return ResponseEntity.ok(portfolioService.getDiversification(pid));
    }

    @GetMapping("/holdings")
    public ResponseEntity<List<PortfolioHolding>> getHoldings(
            @RequestParam(required = false) Long portfolioId) {
        Long pid = portfolioId;
        if (pid == null) {
            Portfolio p = portfolioService.getOrCreateDefault(DEFAULT_USER);
            pid = p.getId();
        }
        return ResponseEntity.ok(portfolioService.getHoldings(pid));
    }
}
