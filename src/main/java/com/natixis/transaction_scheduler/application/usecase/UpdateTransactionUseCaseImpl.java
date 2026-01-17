package com.natixis.transaction_scheduler.application.usecase;

import com.natixis.transaction_scheduler.domain.exception.FeeConfigurationNotFoundException;
import com.natixis.transaction_scheduler.domain.exception.TransactionNotFoundException;
import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import com.natixis.transaction_scheduler.domain.port.in.UpdateTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.out.FeeConfigurationRepository;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Use Case implementation for updating transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionUseCaseImpl implements UpdateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final FeeConfigurationRepository feeConfigurationRepository;

    @Override
    @Transactional
    public Transaction execute(UpdateTransactionCommand command) {

        log.info("Updating transaction ID: {}", command.transactionId());

        AccountNumber sourceAccount = new AccountNumber(command.sourceAccount());
        AccountNumber destinationAccount = new AccountNumber(command.destinationAccount());
        Money transferAmount = new Money(command.transferAmount());
        LocalDate scheduledDate = command.scheduledDate();

        Transaction existingTransaction = transactionRepository.findById(command.transactionId())
                .orElseThrow(() -> new TransactionNotFoundException(
                        String.format("Transaction not found with ID: %d", command.transactionId())
                ));

        log.debug("Existing transaction: {}", existingTransaction.getSummary());

        boolean needsRecalculation =
                !existingTransaction.getTransferAmount().equals(transferAmount) ||
                        !existingTransaction.getScheduledDate().equals(scheduledDate);

        Money newFee;
        FeeConfiguration newFeeConfiguration;

        if (needsRecalculation) {
            long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), scheduledDate);
            newFeeConfiguration = feeConfigurationRepository
                    .findBestMatch(transferAmount, daysBetween)
                    .orElseThrow(() -> new FeeConfigurationNotFoundException(
                            String.format("No fee configuration found for amount %s and %d days",
                                    transferAmount, daysBetween)
                    ));

            log.info("Amount or date changed. Recalculating fee...");

            newFee = newFeeConfiguration.calculateFee(transferAmount);

            log.info("New fee calculated: {} (was: {})", newFee, existingTransaction.getTransferFee());
            log.debug("New fee configuration: {}", newFeeConfiguration.getFeeType());
        } else {
            log.debug("Amount and date unchanged. Keeping existing fee.");
            newFee = existingTransaction.getTransferFee();
            newFeeConfiguration = existingTransaction.getFeeConfiguration();
        }

        Transaction updatedTransaction = existingTransaction.update(
                sourceAccount,
                destinationAccount,
                transferAmount,
                scheduledDate,
                newFee,
                newFeeConfiguration
        );

        Transaction savedTransaction = transactionRepository.save(updatedTransaction);

        log.info("Transaction updated successfully: {}", savedTransaction.getSummary());

        return savedTransaction;
    }
}
