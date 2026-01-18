package com.natixis.transaction_scheduler.domain.port.in;

import com.natixis.transaction_scheduler.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Use Case for updating transactions.
 * Supports both full (PUT) and partial (PATCH) updates.
 */
public interface UpdateTransactionUseCase {

    Transaction execute(UpdateTransactionCommand command);

    /**
     * Command object for updating transactions.
     * Fields are Optional to support partial updates (PATCH).
     *<p>
     * For PUT (full update): all fields must be present
     * For PATCH (partial update): only changed fields are present
     */
    record UpdateTransactionCommand(
            Long transactionId,
            Optional<String> sourceAccount,
            Optional<String> destinationAccount,
            Optional<BigDecimal> transferAmount,
            Optional<LocalDate> scheduledDate
    ) {
        /**
         * Check if any field needs to be updated.
         */
        public boolean hasAnyUpdate() {
            return sourceAccount.isPresent()
                    || destinationAccount.isPresent()
                    || transferAmount.isPresent()
                    || scheduledDate.isPresent();
        }

        /**
         * Check if fee recalculation is needed.
         */
        public boolean needsFeeRecalculation() {
            return transferAmount.isPresent() || scheduledDate.isPresent();
        }
    }
}
