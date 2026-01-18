package com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.entity;

import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.mapper.FeeConfigurationEntityMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing configurable fee calculation rules.
 * Allows dynamic fee configuration without code changes.
 */
@Entity
@Table(name = "fee_configurations", indexes = {
        @Index(name = "idx_fee_type_active", columnList = "fee_type,active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeConfigurationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Fee type identifier (TAXA_A, TAXA_B, TAXA_C)
     */
    @Column(nullable = false, unique = true, length = 20)
    private String feeType;

    /**
     * Minimum transfer amount for this fee type (inclusive)
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minAmount;

    /**
     * Maximum transfer amount for this fee type (inclusive)
     * NULL means no upper limit
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal maxAmount;

    /**
     * Minimum days between current date and scheduled date (inclusive)
     * NULL means no minimum
     */
    @Column(name = "min_days")
    private Integer minDays;

    /**
     * Maximum days between current date and scheduled date (inclusive)
     * NULL means no maximum
     */
    @Column(name = "max_days")
    private Integer maxDays;

    /**
     * Percentage fee to apply (e.g., 0.09 for 9%)
     */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal percentageFee;

    /**
     * Fixed fee amount to add (e.g., 3.00 EUR)
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal fixedFee;

    /**
     * Priority for rule evaluation (lower = higher priority)
     * Used when multiple rules could match
     */
    @Column(nullable = false)
    private Integer priority;

    /**
     * Whether this configuration is active
     */
    @Column(nullable = false)
    private Boolean active;

    /**
     * Description of the fee rule
     */
    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public FeeConfiguration toFeeConfiguration() {
        return FeeConfigurationEntityMapper.INSTANCE.toModel(this);
    }
}
