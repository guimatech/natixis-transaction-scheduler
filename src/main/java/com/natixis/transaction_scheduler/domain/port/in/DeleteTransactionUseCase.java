package com.natixis.transaction_scheduler.domain.port.in;

/**
 * Use Case for deleting a transaction.
 */
public interface DeleteTransactionUseCase {
    void execute(Long id);
}
