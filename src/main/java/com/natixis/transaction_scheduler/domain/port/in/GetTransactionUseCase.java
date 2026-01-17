package com.natixis.transaction_scheduler.domain.port.in;

import com.natixis.transaction_scheduler.domain.model.Transaction;
import java.util.List;

/**
 * Input Port for querying transactions.
 */
public interface GetTransactionUseCase {
    Transaction getById(Long id);
    List<Transaction> getAll();
}