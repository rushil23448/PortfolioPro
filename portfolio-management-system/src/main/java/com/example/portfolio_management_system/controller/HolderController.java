package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.repository.HolderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holders")
@CrossOrigin(origins = "*") // Allows frontend to communicate
public class HolderController {

    private final HolderRepository holderRepository;

    public HolderController(HolderRepository holderRepository) {
        this.holderRepository = holderRepository;
    }

    // ✅ API to Add a Holder
    @PostMapping
    public Holder addHolder(@RequestBody Holder holder) {
        return holderRepository.save(holder);
    }

    // ✅ API to Get All Holders (Alternative to PortfolioController)
    @GetMapping
    public List<Holder> getAllHolders() {
        return holderRepository.findAll();
    }
}