package com.example.portfolio_management_system.repository;

import com.example.portfolio_management_system.model.Holder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolderRepository extends JpaRepository<Holder, Long> {
}
