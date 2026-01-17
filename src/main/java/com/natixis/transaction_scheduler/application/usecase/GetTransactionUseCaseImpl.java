package com.natixis.transaction_scheduler.application.usecase;

import com.natixis.transaction_scheduler.domain.exception.TransactionNotFoundException;
import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.port.in.GetTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
/**
 * Use Case implementation for querying transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetTransactionUseCaseImpl implements GetTransactionUseCase {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public Transaction getById(Long id) {
        log.debug("Fetching transaction by ID: {}", id);

        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(
                        String.format("Transaction not found with ID: %d", id)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAll() {
        log.debug("Fetching all transactions");

        List<Transaction> transactions = transactionRepository.findAll();

        log.info("Found {} transactions", transactions.size());

        return transactions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getByScheduledDate(LocalDate date) {
        log.debug("Fetching transactions scheduled for: {}", date);

        List<Transaction> transactions = transactionRepository.findByScheduledDate(date);

        log.info("Found {} transactions for date {}", transactions.size(), date);

        return transactions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getBySourceAccount(String accountNumber) {
        log.debug("Fetching transactions for source account: {}", accountNumber);

        List<Transaction> transactions = transactionRepository.findBySourceAccount(accountNumber);

        log.info("Found {} transactions for account {}", transactions.size(), accountNumber);

        return transactions;
    }
}