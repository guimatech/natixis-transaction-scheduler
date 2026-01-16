package com.natixis.transaction_scheduler.domain.model;

import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for simplified FeeConfiguration domain model.
 */
@DisplayName("FeeConfiguration Domain Model Tests")
class FeeConfigurationTest {

    @Test
    @DisplayName("Should create fee configuration with valid parameters")
    void shouldCreateFeeConfiguration() {
        // When
        FeeConfiguration config = FeeConfiguration.create(
                "TAXA_A",
                Money.zero(),
                new Money("1000.00"),
                0,
                0,
                new BigDecimal("0.03"),
                new Money("3.00"),
                1,
                "Same day transfer"
        );

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getFeeType()).isEqualTo("TAXA_A");
        assertThat(config.isActive()).isTrue();
        assertThat(config.getPriority()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should match when amount and days are within range")
    void shouldMatchCriteria() {
        // Given
        FeeConfiguration config = FeeConfiguration.create(
                "TAXA_B",
                new Money("1000.01"),
                new Money("2000.00"),
                1,
                10,
                new BigDecimal("0.09"),
                null,
                2,
                "Taxa B"
        );

        // Then
        assertThat(config.matches(new Money("1500.00"), 5)).isTrue();
        assertThat(config.matches(new Money("500.00"), 5)).isFalse();
        assertThat(config.matches(new Money("1500.00"), 15)).isFalse();
    }

    @Test
    @DisplayName("Should calculate fee with percentage and fixed fee")
    void shouldCalculateFeeWithBoth() {
        // Given
        FeeConfiguration config = FeeConfiguration.create(
                "TAXA_A",
                Money.zero(),
                new Money("1000.00"),
                0,
                0,
                new BigDecimal("0.03"),
                new Money("3.00"),
                1,
                "Taxa A"
        );

        // When
        Money fee = config.calculateFee(new Money("500.00"));

        // Then: 500 * 0.03 + 3 = 18.00
        assertThat(fee.getAmount()).isEqualByComparingTo("18.00");
    }

    @Test
    @DisplayName("Should calculate fee with percentage only")
    void shouldCalculateFeePercentageOnly() {
        // Given
        FeeConfiguration config = FeeConfiguration.create(
                "TAXA_B",
                new Money("1000.01"),
                new Money("2000.00"),
                1,
                10,
                new BigDecimal("0.09"),
                null,
                2,
                "Taxa B"
        );

        // When
        Money fee = config.calculateFee(new Money("1500.00"));

        // Then: 1500 * 0.09 = 135.00
        assertThat(fee.getAmount()).isEqualByComparingTo("135.00");
    }

    @Test
    @DisplayName("Should deactivate configuration maintaining immutability")
    void shouldDeactivate() {
        // Given
        FeeConfiguration config = FeeConfiguration.create(
                "TAXA_A",
                Money.zero(),
                null,
                null,
                null,
                new BigDecimal("0.03"),
                null,
                1,
                "Test"
        );

        // When
        FeeConfiguration deactivated = config.deactivate();

        // Then
        assertThat(deactivated.isActive()).isFalse();
        assertThat(config.isActive()).isTrue(); // Original unchanged
    }

    @Test
    @DisplayName("Should generate readable summary")
    void shouldGenerateSummary() {
        // Given
        FeeConfiguration config = FeeConfiguration.create(
                "TAXA_A",
                Money.zero(),
                new Money("1000.00"),
                0,
                0,
                new BigDecimal("0.03"),
                new Money("3.00"),
                1,
                "Test"
        );

        // When
        String summary = config.getSummary();

        // Then
        assertThat(summary)
                .contains("TAXA_A")
             .contains("3.00%")
             .contains("3.00 EUR")
             .contains("0 days");
    }

    @Test
    @DisplayName("Should validate percentage fee range")
    void shouldValidatePercentageFee() {
        // When & Then - percentage > 100%
        assertThatThrownBy(() -> FeeConfiguration.create(
                "INVALID",
                Money.zero(),
                null,
                null,
                null,
                new BigDecimal("1.5"), // 150%
                null,
                1,
                "Test"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 1");

        // When & Then - negative percentage
        assertThatThrownBy(() -> FeeConfiguration.create(
                "INVALID",
                Money.zero(),
                null,
                null,
                null,
                new BigDecimal("-0.1"),
                null,
                1,
                "Test"
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
