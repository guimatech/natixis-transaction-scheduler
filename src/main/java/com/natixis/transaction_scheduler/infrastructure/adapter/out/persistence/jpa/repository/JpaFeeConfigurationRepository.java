package com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.repository;

import com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.entity.FeeConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface JpaFeeConfigurationRepository extends JpaRepository<FeeConfigurationEntity, Long> {

    /**
     * Find best matching fee configuration.
     * Filters by:
     * - Active = true
     * - Amount within min/max range (or no max)
     * - Days within min/max range
     * Orders by priority DESC (highest first)
     *
     * @param amount Transfer amount
     * @param daysBetween Days between now and scheduled date
     * @return List of matching configurations ordered by priority
     */
    @Query("""
            SELECT f FROM FeeConfigurationEntity f
            WHERE f.active = true
              AND f.minAmount <= :amount
              AND (f.maxAmount IS NULL OR f.maxAmount >= :amount)
              AND f.minDays <= :daysBetween
              AND f.maxDays >= :daysBetween
            ORDER BY f.priority DESC
            """)
    List<FeeConfigurationEntity> findBestMatch(
            @Param("amount") BigDecimal amount,
            @Param("daysBetween") long daysBetween
    );
}
