package com.natixis.transaction_scheduler.domain.model;

import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction Aggregate Root.
 *
 * @author Lucas dos Santos Guimarães
 */
@Getter
@Builder
public class Transaction {
    private final Long id;
    private final AccountNumber sourceAccount;
    private final AccountNumber destinationAccount;
    private final Money transferAmount;
    private final Money transferFee;
    private final FeeConfiguration feeConfiguration;
    private final LocalDate scheduledDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Factory method to create a new transaction with calculated fee.
     */
    public static Transaction create(
            AccountNumber sourceAccount,
            AccountNumber destinationAccount,
            Money transferAmount,
            LocalDate scheduledDate,
            Money calculatedFee,
            FeeConfiguration feeConfiguration) {

        validateBusinessRules(sourceAccount, destinationAccount, transferAmount, scheduledDate);

        return Transaction.builder()
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .transferAmount(transferAmount)
                .transferFee(calculatedFee)
                .feeConfiguration(feeConfiguration)
                .scheduledDate(scheduledDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Business rule validation at domain level.
     */
    private static void validateBusinessRules(
            AccountNumber sourceAccount,
            AccountNumber destinationAccount,
            Money transferAmount,
            LocalDate scheduledDate) {

        if (sourceAccount.equals(destinationAccount)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        if (scheduledDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Scheduled date cannot be in the past");
        }

        if (transferAmount.isLessThanOrEqual(Money.zero())) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
    }

    /**
     * Update transaction maintaining domain invariants.
     */
    public Transaction update(
            AccountNumber sourceAccount,
            AccountNumber destinationAccount,
            Money transferAmount,
            LocalDate scheduledDate,
            Money newFee,
            FeeConfiguration newFeeConfiguration) {

        validateBusinessRules(sourceAccount, destinationAccount, transferAmount, scheduledDate);

        return Transaction.builder()
                .id(this.id)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .transferAmount(transferAmount)
                .transferFee(newFee)
                .feeConfiguration(newFeeConfiguration)
                .scheduledDate(scheduledDate)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Calculate total amount (transfer + fee).
     */
    public Money getTotalAmount() {
        return transferAmount.add(transferFee);
    }

    /**
     * Check if transaction is scheduled for today.
     */
    public boolean isScheduledForToday() {
        return scheduledDate.equals(LocalDate.now());
    }

    /**
     * Get days until scheduled execution.
     * Returns 0 for today, negative for past (shouldn't happen due to validation).
     */
    public long getDaysUntilScheduled() {
        return LocalDate.now().until(scheduledDate, java.time.temporal.ChronoUnit.DAYS);
    }

    /**
     * Check if transaction is overdue (scheduled date passed).
     * Defensive check - shouldn't happen due to creation validation.
     */
    public boolean isOverdue() {
        return scheduledDate.isBefore(LocalDate.now());
    }

    /**
     * Get effective fee rate applied (for reporting/audit).
     * Returns percentage as decimal (e.g., 0.09 for 9%).
     */
    public java.math.BigDecimal getEffectiveFeeRate() {
        if (transferAmount.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return java.math.BigDecimal.ZERO;
        }
        return transferFee.getAmount()
                .divide(transferAmount.getAmount(), 4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Get human-readable transaction summary.
     * Useful for logging and debugging.
     */
    public String getSummary() {
        return String.format(
                "Transaction[%d]: %s -> %s | Amount: %s | Fee: %s (%s) | Scheduled: %s",
                id,
                sourceAccount,
                destinationAccount,
                transferAmount,
                transferFee,
                feeConfiguration != null ? feeConfiguration.getFeeType() : "N/A",
                scheduledDate
        );
    }

    /**
     * Check if transaction has associated fee configuration.
     * Important for audit trail and debugging.
     */
    public boolean hasFeeConfiguration() {
        return feeConfiguration != null;
    }
}
