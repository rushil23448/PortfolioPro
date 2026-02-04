package com.example.portfolio_management_system.model;

import jakarta.persistence.*;
import lombok.*;

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
    private String email;
}
