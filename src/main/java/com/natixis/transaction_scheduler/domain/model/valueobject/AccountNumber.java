package com.natixis.transaction_scheduler.domain.model.valueobject;

import lombok.Value;

/**
 * Value Object representing a bank account number.
 */
@Value
public class AccountNumber {
    String value;

    public AccountNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Account number cannot be empty");
        }
        if (value.length() < 3 || value.length() > 50) {
            throw new IllegalArgumentException("Account number must be between 3 and 50 characters");
        }
        this.value = value.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return value;
    }
}

