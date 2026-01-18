package com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.repository;

import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.entity.TransactionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final JpaTransactionRepository jpaTransactionRepository;

    @Override
    public Transaction save(Transaction transaction) {
        return jpaTransactionRepository
                .save(TransactionEntity.of(transaction))
                .toTransaction();
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        Objects.requireNonNull(id, "id cannot be null");
        return this.jpaTransactionRepository.findById(id)
                .map(TransactionEntity::toTransaction);
    }

    @Override
    public List<Transaction> findAll() {
        return this.jpaTransactionRepository.findAll().stream()
                .map(TransactionEntity::toTransaction).toList();
    }

    @Override
    public List<Transaction> findByScheduledDate(LocalDate date) {
        Objects.requireNonNull(date, "date cannot be null");
        return this.jpaTransactionRepository.findByScheduledDate(date).stream()
                .map(TransactionEntity::toTransaction).toList();
    }

    @Override
    public List<Transaction> findBySourceAccount(String accountNumber) {
        Objects.requireNonNull(accountNumber, "accountNumber cannot be null");
        return this.jpaTransactionRepository.findBySourceAccount(accountNumber).stream()
                .map(TransactionEntity::toTransaction).toList();
    }

    @Override
    public void delete(Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction cannot be null");
        this.jpaTransactionRepository.delete(TransactionEntity.of(transaction));
    }

    @Override
    public boolean existsById(Long id) {
        return this.jpaTransactionRepository.existsById(id);
    }
}
