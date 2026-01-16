package com.natixis.transaction_scheduler.domain.model;

import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * FeeConfiguration Aggregate Root.
 * Represents a configurable fee calculation rule stored in database.
 *
 * @author Lucas dos Santos Guimarães
 */
@Getter
@Builder
public class FeeConfiguration {

    private Long id;
    private String feeType;
    private Money minAmount;
    private Money maxAmount;
    private Integer minDays;
    private Integer maxDays;
    private BigDecimal percentageFee;
    private Money fixedFee;
    private Integer priority;
    private Boolean active;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create a new fee configuration.
     * Validates business rules at creation time.
     */
    public static FeeConfiguration create(
            String feeType,
            Money minAmount,
            Money maxAmount,
            Integer minDays,
            Integer maxDays,
            BigDecimal percentageFee,
            Money fixedFee,
            Integer priority,
            String description) {

        validateBusinessRules(feeType, minAmount, percentageFee, priority);

        return FeeConfiguration.builder()
                .feeType(feeType)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .minDays(minDays)
                .maxDays(maxDays)
                .percentageFee(percentageFee)
                .fixedFee(fixedFee)
                .priority(priority)
                .active(true)
                .description(description)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate business rules at domain level.
     */
    private static void validateBusinessRules(
            String feeType,
            Money minAmount,
            BigDecimal percentageFee,
            Integer priority) {

        if (feeType == null || feeType.isBlank()) {
            throw new IllegalArgumentException("Fee type is required");
        }
        if (minAmount == null) {
            throw new IllegalArgumentException("Minimum amount is required");
        }
        if (percentageFee == null) {
            throw new IllegalArgumentException("Percentage fee is required");
        }
        if (percentageFee.compareTo(BigDecimal.ZERO) < 0 || percentageFee.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Percentage fee must be between 0 and 1 (0% to 100%)");
        }
        if (priority == null || priority < 1) {
            throw new IllegalArgumentException("Priority must be at least 1");
        }
    }

    /**
     * Check if this configuration matches the given transfer criteria.
     * Core domain logic for rule matching.
     */
    public boolean matches(Money transferAmount, long daysBetween) {
        if (!Boolean.TRUE.equals(active)) {
            return false;
        }

        // Check amount range
        boolean amountAboveMin = transferAmount.getAmount().compareTo(minAmount.getAmount()) >= 0;
        boolean amountBelowMax = maxAmount == null ||
                transferAmount.getAmount().compareTo(maxAmount.getAmount()) <= 0;

        // Check days range
        boolean daysAboveMin = minDays == null || daysBetween >= minDays;
        boolean daysBelowMax = maxDays == null || daysBetween <= maxDays;

        return amountAboveMin && amountBelowMax && daysAboveMin && daysBelowMax;
    }

    /**
     * Calculate fee using this configuration's rule.
     * Business logic: (amount * percentage) + fixedFee
     */
    public Money calculateFee(Money transferAmount) {
        if (!Boolean.TRUE.equals(active)) {
            throw new IllegalStateException("Cannot calculate fee with inactive configuration");
        }

        // Calculate percentage fee
        Money percentageFeeAmount = transferAmount.multiply(percentageFee);

        // Add fixed fee if present
        if (fixedFee != null) {
            return percentageFeeAmount.add(fixedFee);
        }

        return percentageFeeAmount;
    }

    /**
     * Update configuration maintaining domain invariants.
     */
    public FeeConfiguration update(
            String feeType,
            Money minAmount,
            Money maxAmount,
            Integer minDays,
            Integer maxDays,
            BigDecimal percentageFee,
            Money fixedFee,
            Integer priority,
            String description) {

        validateBusinessRules(feeType, minAmount, percentageFee, priority);

        return FeeConfiguration.builder()
                .id(this.id)
                .feeType(feeType)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .minDays(minDays)
                .maxDays(maxDays)
                .percentageFee(percentageFee)
                .fixedFee(fixedFee)
                .priority(priority)
                .active(this.active)
                .description(description)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Deactivate this configuration.
     */
    public FeeConfiguration deactivate() {
        return toBuilder()
                .active(false)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Activate this configuration.
     */
    public FeeConfiguration activate() {
        return toBuilder()
                .active(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Check if this configuration is currently active.
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    /**
     * Get human-readable summary of this configuration.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(feeType).append(": ");

        // Percentage display
        BigDecimal percentageDisplay = percentageFee.multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
        summary.append(percentageDisplay).append("%");

        // Fixed fee display
        if (fixedFee != null) {
            summary.append(" + ").append(fixedFee);
        }

        // Amount range
        summary.append(" for ").append(minAmount);
        if (maxAmount != null) {
            summary.append(" to ").append(maxAmount);
        } else {
            summary.append("+");
        }

        // Days range
        if (minDays != null || maxDays != null) {
            summary.append(" (");
            if (minDays != null && maxDays != null && minDays.equals(maxDays)) {
                summary.append(minDays).append(" days");
            } else {
                if (minDays != null) {
                    summary.append(minDays).append("+");
                }
                if (maxDays != null) {
                    summary.append(" up to ").append(maxDays);
                }
                summary.append(" days");
            }
            summary.append(")");
        }

        return summary.toString();
    }

    /**
     * Compare priority for ordering.
     * Lower priority number = higher priority.
     */
    public boolean hasHigherPriorityThan(FeeConfiguration other) {
        return this.priority < other.priority;
    }

    /**
     * Create builder from current instance for updates.
     */
    private FeeConfigurationBuilder toBuilder() {
        return FeeConfiguration.builder()
                .id(this.id)
                .feeType(this.feeType)
                .minAmount(this.minAmount)
                .maxAmount(this.maxAmount)
                .minDays(this.minDays)
                .maxDays(this.maxDays)
                .percentageFee(this.percentageFee)
                .fixedFee(this.fixedFee)
                .priority(this.priority)
                .active(this.active)
                .description(this.description)
                .createdAt(this.createdAt);
    }
}
