package com.natixis.transaction_scheduler.infrastructure.adapter.in.dto.request;

import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(

        @NotBlank(message = "Source account is required")
        @Pattern(regexp = AccountNumber.IBAN_FORMAT, message = "Invalid source account format")
        String sourceAccount,

        @NotBlank(message = "Destination account is required")
        @Pattern(regexp = AccountNumber.IBAN_FORMAT, message = "Invalid destination account format")
        String destinationAccount,

        @NotNull(message = "Transfer amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
        BigDecimal transferAmount,

        @NotNull(message = "Scheduled date is required")
        @FutureOrPresent(message = "Scheduled date must be today or in the future")
        LocalDate scheduledDate
) {
}
