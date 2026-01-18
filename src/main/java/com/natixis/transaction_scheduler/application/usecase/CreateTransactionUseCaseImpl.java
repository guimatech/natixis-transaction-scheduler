package com.natixis.transaction_scheduler.application.usecase;

import com.natixis.transaction_scheduler.domain.exception.ResourceNotFoundException;
import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import com.natixis.transaction_scheduler.domain.port.in.CreateTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.out.FeeConfigurationRepository;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Use Case implementation for creating transactions.
 */
@RequiredArgsConstructor
@Slf4j
public class CreateTransactionUseCaseImpl implements CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final FeeConfigurationRepository feeConfigurationRepository;

    @Override

    public Transaction execute(CreateTransactionCommand command) {
        log.info("Creating transaction: {} -> {}",
                command.sourceAccount(), command.destinationAccount());
        AccountNumber sourceAccount = new AccountNumber(command.sourceAccount());
        AccountNumber destinationAccount = new AccountNumber(command.destinationAccount());
        Money transferAmount = new Money(command.transferAmount());
        LocalDate scheduledDate = command.scheduledDate();

        log.info("Creating transaction: {} -> {} | Amount: {} | Date: {}",
                sourceAccount, destinationAccount, transferAmount, scheduledDate);


        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), scheduledDate);
        FeeConfiguration feeConfiguration = feeConfigurationRepository
                .findBestMatch(transferAmount, daysBetween)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No fee configuration found for amount %s and %d days",
                                transferAmount, daysBetween)
                ));

        log.debug("Applied fee configuration: {} (priority: {})",
                feeConfiguration.getFeeType(), feeConfiguration.getPriority());

        Money calculatedFee = feeConfiguration.calculateFee(transferAmount);

        log.debug("Calculated fee: {}", calculatedFee);

        Transaction transaction = Transaction.create(
                sourceAccount,
                destinationAccount,
                transferAmount,
                scheduledDate,
                calculatedFee,
                feeConfiguration
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction created successfully with ID: {}", savedTransaction.getId());
        log.info("Transaction summary: {}", savedTransaction.getSummary());

        return savedTransaction;
    }
}
