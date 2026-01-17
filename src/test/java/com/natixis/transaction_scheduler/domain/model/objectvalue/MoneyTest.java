package com.natixis.transaction_scheduler.domain.model.objectvalue;

import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Money value object.
 */
@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should create money with valid amount")
    void shouldCreateMoneyWithValidAmount() {
        // Given
        BigDecimal amount = new BigDecimal("100.50");

        // When
        Money money = new Money(amount);

        // Then
        assertThat(money.getAmount()).isEqualByComparingTo("100.50");
    }

    @Test
    @DisplayName("Should create money from string")
    void shouldCreateMoneyFromString() {
        // When
        Money money = new Money("250.75");

        // Then
        assertThat(money.getAmount()).isEqualByComparingTo("250.75");
    }

    @Test
    @DisplayName("Should throw exception for null amount")
    void shouldThrowExceptionForNullAmount() {
        // When & Then
        assertThatThrownBy(() -> new Money((BigDecimal) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for negative amount")
    void shouldThrowExceptionForNegativeAmount() {
        // When & Then
        assertThatThrownBy(() -> new Money("-100.00"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount cannot be negative");
    }

    @Test
    @DisplayName("Should round amount to 2 decimal places")
    void shouldRoundAmountTo2DecimalPlaces() {
        // When
        Money money = new Money("100.12345");

        // Then
        assertThat(money.getAmount()).isEqualByComparingTo("100.12");
    }

    @Test
    @DisplayName("Should add two money values")
    void shouldAddTwoMoneyValues() {
        // Given
        Money money1 = new Money("100.50");
        Money money2 = new Money("50.25");

        // When
        Money result = money1.add(money2);

        // Then
        assertThat(result.getAmount()).isEqualByComparingTo("150.75");
    }

    @Test
    @DisplayName("Should subtract two money values")
    void shouldSubtractTwoMoneyValues() {
        // Given
        Money money1 = new Money("100.00");
        Money money2 = new Money("30.50");

        // When
        Money result = money1.subtract(money2);

        // Then
        assertThat(result.getAmount()).isEqualByComparingTo("69.50");
    }

    @Test
    @DisplayName("Should multiply money by multiplier")
    void shouldMultiplyMoneyByMultiplier() {
        // Given
        Money money = new Money("100.00");
        BigDecimal multiplier = new BigDecimal("0.09");

        // When
        Money result = money.multiply(multiplier);

        // Then
        assertThat(result.getAmount()).isEqualByComparingTo("9.00");
    }

    @Test
    @DisplayName("Should compare money values")
    void shouldCompareMoneyValues() {
        // Given
        Money smaller = new Money("50.00");
        Money larger = new Money("100.00");

        // Then
        assertThat(larger.isGreaterThan(smaller)).isTrue();
        assertThat(smaller.isGreaterThan(larger)).isFalse();
        assertThat(smaller.isLessThanOrEqual(larger)).isTrue();
    }

    @Test
    @DisplayName("Should create zero money")
    void shouldCreateZeroMoney() {
        // When
        Money zero = Money.zero();

        // Then
        assertThat(zero.getAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        Money original = new Money("100.00");

        // When
        Money result = original.add(new Money("50.00"));

        // Then
        assertThat(original.getAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Should format to string with currency")
    void shouldFormatToStringWithCurrency() {
        // Given
        Money money = new Money("1000.50");

        // When
        String formatted = money.toString();

        // Then
        assertThat(formatted).isEqualTo("1000.50 EUR");
    }
}
