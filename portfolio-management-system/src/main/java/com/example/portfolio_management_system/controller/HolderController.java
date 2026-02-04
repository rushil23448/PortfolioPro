package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.service.HolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/holders")
@RequiredArgsConstructor
public class HolderController {

    private final HolderService holderService;

    @PostMapping("/add")
    public Holder addHolder(@RequestBody Holder holder) {
        return holderService.addHolder(holder);
    }

    @GetMapping
    public List<Holder> getAllHolders() {
        return holderService.getAllHolders();
    }

    @GetMapping("/{id}")
    public Holder getHolder(@PathVariable Long id) {
        return holderService.getHolderById(id);
    }
}
