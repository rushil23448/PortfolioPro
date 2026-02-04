package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.repository.HoldingRepository;
import com.example.portfolio_management_system.repository.HolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final HolderRepository holderRepository;

    public Holding addHolding(Long holderId, Holding holding) {

        Holder holder = holderRepository.findById(holderId)
                .orElseThrow(() -> new RuntimeException("Holder not found"));

        holding.setHolder(holder);

        return holdingRepository.save(holding);
    }

    public List<Holding> getHoldingsByHolder(Long holderId) {
        return holdingRepository.findByHolderId(holderId);
    }
}
