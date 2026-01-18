package com.natixis.transaction_scheduler.application.usecase;

import com.natixis.transaction_scheduler.domain.exception.ResourceNotFoundException;
import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import com.natixis.transaction_scheduler.domain.port.in.UpdateTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.out.FeeConfigurationRepository;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Use Case implementation for updating transactions.
 * Supports both full (PUT) and partial (PATCH) updates.
 */
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionUseCaseImpl implements UpdateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final FeeConfigurationRepository feeConfigurationRepository;

    @Override
    public Transaction execute(UpdateTransactionCommand command) {

        log.info("Updating transaction ID: {} (partial: {})",
                command.transactionId(),
                !command.hasAnyUpdate() ? "none" : "yes");

        Transaction existingTransaction = transactionRepository.findById(command.transactionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Transaction not found with ID: %d", command.transactionId())
                ));

        log.debug("Existing transaction: {}", existingTransaction.getSummary());

        AccountNumber sourceAccount = command.sourceAccount()
                .map(AccountNumber::new)
                .orElse(existingTransaction.getSourceAccount());

        AccountNumber destinationAccount = command.destinationAccount()
                .map(AccountNumber::new)
                .orElse(existingTransaction.getDestinationAccount());

        Money transferAmount = command.transferAmount()
                .map(Money::new)
                .orElse(existingTransaction.getTransferAmount());

        LocalDate scheduledDate = command.scheduledDate()
                .orElse(existingTransaction.getScheduledDate());

        // Check if fee recalculation is needed
        boolean needsRecalculation =
                (command.transferAmount().isPresent() &&
                        !existingTransaction.getTransferAmount().equals(transferAmount)) ||
                        (command.scheduledDate().isPresent() &&
                                !existingTransaction.getScheduledDate().equals(scheduledDate));

        Money newFee;
        FeeConfiguration newFeeConfiguration;

        if (needsRecalculation) {
            log.info("Amount or date changed. Recalculating fee...");

            long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), scheduledDate);
            newFeeConfiguration = feeConfigurationRepository
                    .findBestMatch(transferAmount, daysBetween)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("No fee configuration found for amount %s and %d days",
                                    transferAmount, daysBetween)
                    ));

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
