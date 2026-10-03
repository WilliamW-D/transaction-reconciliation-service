package com.reconciliation.repository;

import com.reconciliation.entity.ToleranceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToleranceConfigRepository extends JpaRepository<ToleranceConfig, Long> {
    Optional<ToleranceConfig> findFirstByIsDefaultTrue();
    Optional<ToleranceConfig> findByName(String name);
}
