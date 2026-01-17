package com.natixis.transaction_scheduler.domain.port.in;

import com.natixis.transaction_scheduler.domain.model.Transaction;

import java.time.LocalDate;
import java.util.List;

/**
 * Input Port for querying transactions.
 */
public interface GetTransactionUseCase {

    /**
     * Get transaction by ID.
     */
    Transaction getById(Long id);

    /**
     * Get all transactions.
     */
    List<Transaction> getAll();

    /**
     * Get transactions by scheduled date.
     */
    List<Transaction> getByScheduledDate(LocalDate date);

    /**
     * Get transactions by source account.
     */
    List<Transaction> getBySourceAccount(String accountNumber);
}