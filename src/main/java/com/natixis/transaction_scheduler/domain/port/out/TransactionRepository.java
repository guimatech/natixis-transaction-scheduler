package com.natixis.transaction_scheduler.domain.port.out;

import com.natixis.transaction_scheduler.domain.model.Transaction;
import java.util.List;
import java.util.Optional;

/**
 * Output Port for transaction persistence.
 */
public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(Long id);
    List<Transaction> findAll();
    void delete(Transaction transaction);
    boolean existsById(Long id);
}
