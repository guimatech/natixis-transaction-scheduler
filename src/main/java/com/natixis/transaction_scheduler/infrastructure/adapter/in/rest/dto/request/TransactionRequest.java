package com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.dto.request;

import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request object for creating or updating a transaction")
public record TransactionRequest(

        @Schema(
                description = "Source account number",
                example = "PT50000201231234567890154",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Source account is required")
        @Pattern(regexp = AccountNumber.IBAN_FORMAT, message = "Invalid source account format")
        String sourceAccount,

        @Schema(
                description = "Destination account number",
                example = "DE89370400440532013000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Destination account is required")
        @Pattern(regexp = AccountNumber.IBAN_FORMAT, message = "Invalid destination account format")
        String destinationAccount,

        @Schema(
                description = "Amount to transfer in EUR",
                example = "1500.00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Transfer amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
        BigDecimal transferAmount,

        @Schema(
                description = "Date when the transaction should be executed",
                example = "2026-01-20",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Scheduled date is required")
        @FutureOrPresent(message = "Scheduled date must be today or in the future")
        LocalDate scheduledDate
) {
}
