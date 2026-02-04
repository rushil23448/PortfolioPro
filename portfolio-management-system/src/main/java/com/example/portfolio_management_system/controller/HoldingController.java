package com.example.portfolio_management_system.controller;

import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.service.HoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/holdings")
@RequiredArgsConstructor
public class HoldingController {

    private final HoldingService holdingService;

    @PostMapping("/add/{holderId}")
    public Holding addHolding(@PathVariable Long holderId,
                              @RequestBody Holding holding) {
        return holdingService.addHolding(holderId, holding);
    }

    @GetMapping("/{holderId}")
    public List<Holding> getHoldings(@PathVariable Long holderId) {
        return holdingService.getHoldingsByHolder(holderId);
    }
}
