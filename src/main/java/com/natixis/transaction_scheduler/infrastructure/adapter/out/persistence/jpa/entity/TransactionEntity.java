package com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.entity;

import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.mapper.TransactionEntityMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction entity representing a scheduled bank transfer.
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_scheduled_date", columnList = "scheduled_date"),
        @Index(name = "idx_source_account", columnList = "source_account")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Supports IBAN format (up to 34 characters for European banks).
     * <a href="https://www.bportugal.pt/sites/default/files/anexos/documentos-relacionados/international_bank_account_number_en.pdf">...</a>
     **/
    @NotBlank(message = "Source account is required")
    @Column(name = "source_account", nullable = false, length = 34)
    private String sourceAccount;

    /**
     * Supports IBAN format (up to 34 characters for European banks).
     * <a href="https://www.bportugal.pt/sites/default/files/anexos/documentos-relacionados/international_bank_account_number_en.pdf">...</a>
     **/
    @NotBlank(message = "Destination account is required")
    @Column(name = "destination_account", nullable = false, length = 34)
    private String destinationAccount;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    @Column(name = "transfer_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal transferAmount;

    @Column(name = "transfer_fee", precision = 19, scale = 2)
    private BigDecimal transferFee;

    @NotNull(message = "Scheduled date is required")
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static TransactionEntity of(Transaction transaction) {
        return TransactionEntityMapper.INSTANCE.toEntity(transaction);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Transaction toTransaction() {
        return TransactionEntityMapper.INSTANCE.toModel(this);
    }
}
