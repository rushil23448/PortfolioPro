package com.example.portfolio_management_system.service;

import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.repository.HolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HolderService {

    private final HolderRepository holderRepository;

    public Holder addHolder(Holder holder) {
        return holderRepository.save(holder);
    }

    public List<Holder> getAllHolders() {
        return holderRepository.findAll();
    }

    public Holder getHolderById(Long id) {
        return holderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holder not found"));
    }
}
