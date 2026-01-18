package com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.repository;

import com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findByScheduledDate(LocalDate date);

    List<TransactionEntity> findBySourceAccount(String accountNumber);
}
