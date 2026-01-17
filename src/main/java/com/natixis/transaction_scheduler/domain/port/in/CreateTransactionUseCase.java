package com.natixis.transaction_scheduler.domain.port.in;

import com.natixis.transaction_scheduler.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Input Port for creating transactions.
 */
public interface CreateTransactionUseCase {

    /**
     * Create a new transaction with automatic fee calculation.
     *
     * @param command the command containing transaction details
     * @return the created transaction
     */
    Transaction execute(CreateTransactionCommand command);

    record CreateTransactionCommand(
            String sourceAccount,
            String destinationAccount,
            BigDecimal transferAmount,
            LocalDate scheduledDate
    ) {}
}