package com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(

        Long id,
        String sourceAccount,
        String destinationAccount,
        BigDecimal transferAmount,
        BigDecimal transferFee,
        BigDecimal totalAmount,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate scheduledDate,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {}
