package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.repository.HolderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holders")
@CrossOrigin("*")
public class HolderController {

    private final HolderRepository holderRepository;

    public HolderController(HolderRepository holderRepository) {
        this.holderRepository = holderRepository;
    }

    // ✅ Get all holders
    @GetMapping
    public List<Holder> getAllHolders() {
        return holderRepository.findAll();
    }
}
