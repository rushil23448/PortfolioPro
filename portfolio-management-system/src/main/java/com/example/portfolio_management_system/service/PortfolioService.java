package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.dto.PortfolioItemDTO;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;

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
}
