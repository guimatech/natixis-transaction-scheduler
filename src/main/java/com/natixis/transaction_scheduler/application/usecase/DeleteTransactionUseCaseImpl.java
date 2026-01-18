package com.natixis.transaction_scheduler.application.usecase;

import com.natixis.transaction_scheduler.domain.exception.ResourceNotFoundException;
import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.port.in.DeleteTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DeleteTransactionUseCaseImpl implements DeleteTransactionUseCase {

    private final TransactionRepository transactionRepository;

    @Override
    public void execute(Long id) {
        log.info("Deleting transaction with ID: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Cannot delete. Transaction not found with ID: %d", id)
                ));

        log.debug("Found transaction to delete: {}", transaction.getSummary());

        transactionRepository.delete(transaction);

        log.info("Transaction deleted successfully: ID {}", id);
    }
}