package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.dto.PortfolioItemDTO;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.repository.HoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PortfolioService {

    private final HoldingRepository holdingRepository;

    public PortfolioService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    @Transactional(readOnly = true)
    public List<PortfolioItemDTO> getPortfolioByHolder(Long holderId) {
        List<Holding> holdings = holdingRepository.findByHolderId(holderId);

        return holdings.stream().map(h ->
                new PortfolioItemDTO(
                        h.getStock().getSymbol(),
                        h.getStock().getName(),
                        h.getStock().getSector(),
                        h.getStock().getBasePrice(),
                        h.getQuantity(),
                        h.getQuantity() * h.getStock().getBasePrice()
                )
        ).toList();
    }

    @Transactional(readOnly = true)
    public Double calculateTotalValue(Long holderId) {
        return holdingRepository.findByHolderId(holderId).stream()
                .mapToDouble(h -> h.getStock().getCurrentPrice() * h.getQuantity())
                .sum();
    }

    @Transactional(readOnly = true)
    public Double calculateTotalInvested(Long holderId) {
        return holdingRepository.findByHolderId(holderId).stream()
                .mapToDouble(h -> h.getAvgPrice().doubleValue() * h.getQuantity())
                .sum();
    }
}

