package com.natixis.transaction_scheduler.domain.port.in;

import com.natixis.transaction_scheduler.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Use Case for updating an existing transaction.
 * Recalculates fee if amount or date changes.
 */
public interface UpdateTransactionUseCase {

    Transaction execute(UpdateTransactionCommand command);

    record UpdateTransactionCommand(
            Long transactionId,
            String sourceAccount,
            String destinationAccount,
            BigDecimal transferAmount,
            LocalDate scheduledDate
    ) {}
}
