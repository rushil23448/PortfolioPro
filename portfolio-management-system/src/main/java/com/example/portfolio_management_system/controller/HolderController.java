package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.service.HolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HolderController {

    private final HolderService holderService;

    @GetMapping
    public List<Holder> getAllHolders() {
        return holderService.getAllHolders();
    }

    @PostMapping
    public Holder createHolder(@RequestBody Holder holder) {
        return holderService.addHolder(holder);
    }
}