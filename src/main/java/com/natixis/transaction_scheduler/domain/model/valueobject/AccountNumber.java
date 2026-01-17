package com.natixis.transaction_scheduler.domain.model.valueobject;

import lombok.Value;

/**
 * Account Number Value Object.
 * Supports IBAN format (up to 34 characters for European banks).
 * <a href="https://www.bportugal.pt/sites/default/files/anexos/documentos-relacionados/international_bank_account_number_en.pdf">...</a>
 * <p>
 * Example IBANs:<br />
 * - France: FR76 3000 6000 0112 3456 7890 189 (27 chars)<br />
 * - Portugal: PT50 0002 0123 1234 5678 9015 4 (25 chars)<br />
 * - Germany: DE89 3704 0044 0532 0130 00 (22 chars)<br />
 */
@Value
public class AccountNumber {

    public static final String IBAN_FORMAT = "^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$";

    String value;

    public AccountNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Account number cannot be empty");
        }

        // Remove spaces and normalize
        String normalized = value.replaceAll("\\s+", "").toUpperCase();

        // IBAN validation: 15-34 alphanumeric characters
        if (!normalized.matches(IBAN_FORMAT)) {
            throw new IllegalArgumentException(
                    "Invalid IBAN format. Expected format: XX00XXXXXXXXXXX... (15-34 characters)"
            );
        }

        this.value = normalized;
    }

    /**
     * Format IBAN with spaces for display (every 4 characters).
     * Example: FR7630006000011234567890189 -> FR76 3000 6000 0112 3456 7890 189
     */
    public String getFormattedValue() {
        return value.replaceAll("(.{4})", "$1 ").trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
