package com.natixis.transaction_scheduler.domain.model;

import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Transaction aggregate root.
 */
@DisplayName("Transaction Domain Model Tests")
class TransactionTest {

    public static final String ACCOUNT_ID_FRANCE = "FR7630006000011234567890189";
    public static final String ACCOUNT_ID_PORTUGAL = "PT50000201231234567890154";
    public static final String ACCOUNT_ID_GERMANY = "DE89370400440532013000";

    @Test
    @DisplayName("Should create transaction with valid parameters")
    void shouldCreateTransaction() {
        // Given
        AccountNumber source = new AccountNumber(ACCOUNT_ID_FRANCE);
        AccountNumber destination = new AccountNumber(ACCOUNT_ID_PORTUGAL);
        Money amount = new Money("500.00");
        Money fee = new Money("18.00");
        LocalDate scheduledDate = LocalDate.now().plusDays(1);

        FeeConfiguration feeConfig = FeeConfiguration.create(
                "TAXA_A",
                Money.zero(),
                new Money("1000.00"),
                0, 0,
                new BigDecimal("0.03"),
                new Money("3.00"),
                1,
                "Test"
        );

        // When
        Transaction transaction = Transaction.create(
                source, destination, amount, scheduledDate, fee, feeConfig
        );

        // Then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getSourceAccount()).isEqualTo(source);
        assertThat(transaction.getDestinationAccount()).isEqualTo(destination);
        assertThat(transaction.getTransferAmount()).isEqualTo(amount);
        assertThat(transaction.getTransferFee()).isEqualTo(fee);
        assertThat(transaction.getScheduledDate()).isEqualTo(scheduledDate);
        assertThat(transaction.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when source and destination are the same")
    void shouldThrowExceptionForSameAccounts() {
        // Given
        AccountNumber sameAccount = new AccountNumber(ACCOUNT_ID_FRANCE);
        Money amount = new Money("500.00");
        Money fee = new Money("18.00");

        // When & Then
        assertThatThrownBy(() -> Transaction.create(
                sameAccount, sameAccount, amount, LocalDate.now(), fee, null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source and destination accounts cannot be the same");
    }

    @Test
    @DisplayName("Should throw exception for past scheduled date")
    void shouldThrowExceptionForPastDate() {
        // Given
        AccountNumber source = new AccountNumber(ACCOUNT_ID_FRANCE);
        AccountNumber destination = new AccountNumber(ACCOUNT_ID_PORTUGAL);
        Money amount = new Money("500.00");
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThatThrownBy(() -> Transaction.create(
                source, destination, amount, pastDate, Money.zero(), null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scheduled date cannot be in the past");
    }

    @Test
    @DisplayName("Should throw exception for zero or negative amount")
    void shouldThrowExceptionForInvalidAmount() {
        // Given
        AccountNumber source = new AccountNumber(ACCOUNT_ID_FRANCE);
        AccountNumber destination = new AccountNumber(ACCOUNT_ID_PORTUGAL);

        // When & Then
        assertThatThrownBy(() -> Transaction.create(
                source, destination, Money.zero(), LocalDate.now(), Money.zero(), null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transfer amount must be greater than zero");
    }

    @Test
    @DisplayName("Should calculate total amount correctly")
    void shouldCalculateTotalAmount() {
        // Given
        Transaction transaction = Transaction.create(
                new AccountNumber(ACCOUNT_ID_FRANCE),
                new AccountNumber(ACCOUNT_ID_PORTUGAL),
                new Money("500.00"),
                LocalDate.now(),
                new Money("18.00"),
                null
        );

        // When
        Money total = transaction.getTotalAmount();

        // Then: 500 + 18 = 518
        assertThat(total.getAmount()).isEqualByComparingTo("518.00");
    }

    @Test
    @DisplayName("Should check if scheduled for today")
    void shouldCheckIfScheduledForToday() {
        // Given
        Transaction todayTransaction = Transaction.create(
                new AccountNumber(ACCOUNT_ID_FRANCE),
                new AccountNumber(ACCOUNT_ID_PORTUGAL),
                new Money("500.00"),
                LocalDate.now(),
                new Money("18.00"),
                null
        );

        Transaction futureTransaction = Transaction.create(
                new AccountNumber(ACCOUNT_ID_FRANCE),
                new AccountNumber(ACCOUNT_ID_PORTUGAL),
                new Money("500.00"),
                LocalDate.now().plusDays(5),
                new Money("18.00"),
                null
        );

        // Then
        assertThat(todayTransaction.isScheduledForToday()).isTrue();
        assertThat(futureTransaction.isScheduledForToday()).isFalse();
    }

    @Test
    @DisplayName("Should calculate days until scheduled")
    void shouldCalculateDaysUntilScheduled() {
        // Given
        Transaction transaction = Transaction.create(
                new AccountNumber(ACCOUNT_ID_FRANCE),
                new AccountNumber(ACCOUNT_ID_PORTUGAL),
                new Money("500.00"),
                LocalDate.now().plusDays(5),
                new Money("18.00"),
                null
        );

        // Then
        assertThat(transaction.getDaysUntilScheduled()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should calculate effective fee rate")
    void shouldCalculateEffectiveFeeRate() {
        // Given
        Transaction transaction = Transaction.create(
                new AccountNumber(ACCOUNT_ID_FRANCE),
                new AccountNumber(ACCOUNT_ID_PORTUGAL),
                new Money("1000.00"),
                LocalDate.now(),
                new Money("90.00"), // 9%
                null
        );

        // When
        BigDecimal rate = transaction.getEffectiveFeeRate();

        // Then
        assertThat(rate).isEqualByComparingTo("0.0900");
    }

    @Test
    @DisplayName("Should update transaction maintaining immutability")
    void shouldUpdateTransaction() {
        // Given
        Transaction original = Transaction.create(
                new AccountNumber(ACCOUNT_ID_FRANCE),
                new AccountNumber(ACCOUNT_ID_PORTUGAL),
                new Money("500.00"),
                LocalDate.now().plusDays(1),
                new Money("18.00"),
                null
        );

        // When
        Transaction updated = original.update(
                new AccountNumber(ACCOUNT_ID_FRANCE),
                new AccountNumber(ACCOUNT_ID_GERMANY), // Different destination
                new Money("1000.00"), // Different amount
                LocalDate.now().plusDays(2),
                new Money("36.00"),
                null
        );

        // Then
        assertThat(updated.getDestinationAccount().getValue()).isEqualTo(ACCOUNT_ID_GERMANY);
        assertThat(updated.getTransferAmount().getAmount()).isEqualByComparingTo("1000.00");
        assertThat(original.getDestinationAccount().getValue()).isEqualTo(ACCOUNT_ID_PORTUGAL); // Unchanged
    }

    @Test
    @DisplayName("Should generate human-readable summary")
    void shouldGenerateSummary() {
        // Given
        FeeConfiguration feeConfig = FeeConfiguration.create(
                "TAXA_A",
                Money.zero(), null, null, null,
                new BigDecimal("0.03"), null, 1, "Test"
        );

        Transaction transaction = Transaction.builder()
                .id(1L)
                .sourceAccount(new AccountNumber(ACCOUNT_ID_FRANCE))
                .destinationAccount(new AccountNumber(ACCOUNT_ID_PORTUGAL))
                .transferAmount(new Money("500.00"))
                .transferFee(new Money("18.00"))
                .feeConfiguration(feeConfig)
                .scheduledDate(LocalDate.now())
                .build();

        // When
        String summary = transaction.getSummary();

        // Then
        assertThat(summary).contains("Transaction[1]");
        assertThat(summary).contains(ACCOUNT_ID_FRANCE);
        assertThat(summary).contains(ACCOUNT_ID_PORTUGAL);
        assertThat(summary).contains("500.00");
        assertThat(summary).contains("18.00");
        assertThat(summary).contains("TAXA_A");
    }

    @Test
    @DisplayName("Should check if has fee configuration")
    void shouldCheckIfHasFeeConfiguration() {
        // Given
        Transaction withConfig = Transaction.create(
                new AccountNumber(ACCOUNT_ID_FRANCE),
                new AccountNumber(ACCOUNT_ID_PORTUGAL),
                new Money("500.00"),
                LocalDate.now(),
                new Money("18.00"),
                FeeConfiguration.create(
                        "TAXA_A", Money.zero(), null, null, null,
                        new BigDecimal("0.03"), null, 1, "Test"
                )
        );

        Transaction withoutConfig = Transaction.create(
                new AccountNumber(ACCOUNT_ID_FRANCE),
                new AccountNumber(ACCOUNT_ID_PORTUGAL),
                new Money("500.00"),
                LocalDate.now(),
                new Money("18.00"),
                null
        );

        // Then
        assertThat(withConfig.hasFeeConfiguration()).isTrue();
        assertThat(withoutConfig.hasFeeConfiguration()).isFalse();
    }
}
