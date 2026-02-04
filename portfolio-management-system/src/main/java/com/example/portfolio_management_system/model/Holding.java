package com.example.portfolio_management_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "holdings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockSymbol;
    private Integer quantity;
    private Double avgPrice;

    @ManyToOne
    @JoinColumn(name = "holder_id")
    private Holder holder;
}
