package com.example.portfolio_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "holders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // ✅ Prevent Infinite Loop
    @OneToMany(mappedBy = "holder", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Holding> holdings;
}
