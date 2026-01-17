package com.natixis.transaction_scheduler.domain.model.objectvalue;

import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Account Number Value Object Tests")
class AccountNumberTest {

    @Test
    @DisplayName("Should create account number with valid value")
    void shouldCreateAccountNumberWithValidValue() {
        // When
        AccountNumber account = new AccountNumber("PT50000201231234567890154");

        // Then
        assertThat(account.getValue()).isEqualTo("PT50000201231234567890154");
    }

    @Test
    @DisplayName("Should normalize account number to uppercase")
    void shouldNormalizeToUppercase() {
        // When
        AccountNumber account = new AccountNumber("pt50000201231234567890154");

        // Then
        assertThat(account.getValue()).isEqualTo("PT50000201231234567890154");
    }

    @Test
    @DisplayName("Should trim whitespace")
    void shouldTrimWhitespace() {
        // When
        AccountNumber account = new AccountNumber("  FR76 3000 6000 0112 3456 7890 189  ");

        // Then
        assertThat(account.getValue()).isEqualTo("FR7630006000011234567890189");
    }

    @Test
    @DisplayName("Should throw exception for null value")
    void shouldThrowExceptionForNullValue() {
        // When & Then
        assertThatThrownBy(() -> new AccountNumber(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception for empty value")
    void shouldThrowExceptionForEmptyValue() {
        // When & Then
        assertThatThrownBy(() -> new AccountNumber(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception for blank value")
    void shouldThrowExceptionForBlankValue() {
        // When & Then
        assertThatThrownBy(() -> new AccountNumber("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception for too short value")
    void shouldThrowExceptionForTooShortValue() {
        // When & Then
        assertThatThrownBy(() -> new AccountNumber("AB"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid IBAN format. Expected format: XX00XXXXXXXXXXX... (15-34 characters)");
    }

    @Test
    @DisplayName("Should throw exception for too long value")
    void shouldThrowExceptionForTooLongValue() {
        // When & Then
        String tooLong = "A".repeat(35);
        assertThatThrownBy(() -> new AccountNumber(tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid IBAN format. Expected format: XX00XXXXXXXXXXX... (15-34 characters)");
    }

    @Test
    @DisplayName("Should be equal when values are same")
    void shouldBeEqualWhenValuesAreSame() {
        // Given
        AccountNumber account1 = new AccountNumber("DE89 3704 0044 0532 0130 00");
        AccountNumber account2 = new AccountNumber("de89 3704 0044 0532 0130 00");

        // Then
        assertThat(account1).isEqualTo(account2);
    }

    @Test
    @DisplayName("Should have same hash code when values are same")
    void shouldHaveSameHashCodeWhenValuesAreSame() {
        // Given
        AccountNumber account1 = new AccountNumber("DE89 3704 0044 0532 0130 00");
        AccountNumber account2 = new AccountNumber("de89 3704 0044 0532 0130 00");

        // Then
        assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
    }
}