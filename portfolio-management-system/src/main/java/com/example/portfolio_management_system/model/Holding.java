package com.example.portfolio_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private Integer quantity;
    private Double avgPrice;

    // ✅ Stock Object Relation
    @ManyToOne
    @JoinColumn(name = "stock_symbol")
    private Stock stock;

    // ✅ Holder Relation
    @ManyToOne
    @JoinColumn(name = "holder_id")
    @JsonIgnore
    private Holder holder;
}
