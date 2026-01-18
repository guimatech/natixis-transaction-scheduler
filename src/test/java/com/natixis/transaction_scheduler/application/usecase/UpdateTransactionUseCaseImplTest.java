package com.natixis.transaction_scheduler.application.usecase;

import com.natixis.transaction_scheduler.domain.exception.ResourceNotFoundException;
import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import com.natixis.transaction_scheduler.domain.port.in.UpdateTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.out.FeeConfigurationRepository;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateTransactionUseCase Tests")
class UpdateTransactionUseCaseImplTest {

    private static final String ACCOUNT_ID_FRANCE = "FR7630006000011234567890189";
    private static final String ACCOUNT_ID_PORTUGAL = "PT50000201231234567890154";
    private static final String ACCOUNT_ID_GERMANY = "DE89370400440532013000";
    private static final String ACCOUNT_ID_AUSTRIA = "AT483200000012345864";

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FeeConfigurationRepository feeConfigurationRepository;

    @InjectMocks
    private UpdateTransactionUseCaseImpl updateTransactionUseCase;

    private Transaction existingTransaction;
    private FeeConfiguration feeConfiguration;

    @BeforeEach
    void setUp() {
        feeConfiguration = FeeConfiguration.builder()
                .id(1L)
                .feeType("TAXA_B")
                .minAmount(new Money("1000.01"))
                .maxAmount(new Money("2000.00"))
                .minDays(1)
                .maxDays(10)
                .percentageFee(new BigDecimal("0.09"))
                .priority(2)
                .active(true)
                .build();

        existingTransaction = Transaction.builder()
                .id(1L)
                .sourceAccount(new AccountNumber(ACCOUNT_ID_FRANCE))
                .destinationAccount(new AccountNumber(ACCOUNT_ID_PORTUGAL))
                .transferAmount(new Money("500.00"))
                .transferFee(new Money("18.00"))
                .feeConfiguration(feeConfiguration)
                .scheduledDate(LocalDate.now())
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("Should update transaction successfully with fee recalculation")
    void shouldUpdateTransactionWithFeeRecalculation() {
        // Given - New amount requires fee recalculation
        BigDecimal newAmount = BigDecimal.valueOf(1500.00);
        Money newFee = new Money("135.00");
        LocalDate newDate = LocalDate.now().plusDays(5);

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(existingTransaction));
        when(feeConfigurationRepository.findBestMatch(any(), anyLong()))
                .thenReturn(Optional.of(feeConfiguration));
        when(transactionRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        Transaction result = updateTransactionUseCase.execute(
                new UpdateTransactionUseCase.UpdateTransactionCommand(
                        1L,
                        Optional.of(ACCOUNT_ID_FRANCE),
                        Optional.of(ACCOUNT_ID_PORTUGAL),
                        Optional.of(newAmount),
                        Optional.of(newDate)
        ));

        // Then
        assertThat(result.getTransferAmount().getAmount()).isEqualTo(newAmount.setScale(2, RoundingMode.HALF_UP));
        assertThat(result.getTransferFee()).isEqualTo(newFee);
        assertThat(result.getScheduledDate()).isEqualTo(newDate);

        verify(feeConfigurationRepository).findBestMatch(any(), anyLong());
        verify(transactionRepository).save(any());
    }

    @Test
    @DisplayName("Should update without fee recalculation when amount and date unchanged")
    void shouldUpdateWithoutFeeRecalculation() {
        // Given - Only accounts changed, amount and date same
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(existingTransaction));
        when(transactionRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        Transaction result = updateTransactionUseCase.execute(
                new UpdateTransactionUseCase.UpdateTransactionCommand(
                        1L,
                        Optional.of(ACCOUNT_ID_GERMANY), // Changed
                        Optional.of(ACCOUNT_ID_AUSTRIA), // Changed
                        Optional.of(existingTransaction.getTransferAmount().getAmount()), // Same
                        Optional.of(existingTransaction.getScheduledDate()) // Same
        ));

        // Then
        assertThat(result.getSourceAccount().getValue()).isEqualTo(ACCOUNT_ID_GERMANY);
        assertThat(result.getDestinationAccount().getValue()).isEqualTo(ACCOUNT_ID_AUSTRIA);
        assertThat(result.getTransferFee()).isEqualTo(existingTransaction.getTransferFee());

        verify(feeConfigurationRepository, never()).findBestMatch(any(), anyLong());
        verify(transactionRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when transaction not found")
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Given
        when(transactionRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> updateTransactionUseCase.execute(
                        new UpdateTransactionUseCase.UpdateTransactionCommand(
                                999L,
                                Optional.of(ACCOUNT_ID_FRANCE),
                                Optional.of(ACCOUNT_ID_PORTUGAL),
                                Optional.of(BigDecimal.valueOf(500.00)),
                                Optional.of(LocalDate.now())
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with ID: 999");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should recalculate fee when only amount changes")
    void shouldRecalculateFeeWhenAmountChanges() {
        // Given - Date same, amount changed
        BigDecimal newAmount = new BigDecimal("2500.00");
        BigDecimal newFee = new BigDecimal("225.00");

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(existingTransaction));
        when(feeConfigurationRepository.findBestMatch(any(), anyLong()))
                .thenReturn(Optional.of(feeConfiguration));
        when(transactionRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        Transaction result = updateTransactionUseCase.execute(
                new UpdateTransactionUseCase.UpdateTransactionCommand(
                        1L,
                        Optional.of(existingTransaction.getSourceAccount().getValue()),
                        Optional.of(existingTransaction.getDestinationAccount().getValue()),
                        Optional.of(newAmount), // Changed
                        Optional.of(existingTransaction.getScheduledDate()) // Same
        ));

        // Then
        assertThat(result.getTransferAmount().getAmount()).isEqualTo(newAmount);
        assertThat(result.getTransferFee().getAmount()).isEqualTo(newFee);
    }

    @Test
    @DisplayName("Should recalculate fee when only date changes")
    void shouldRecalculateFeeWhenDateChanges() {
        // Given - Amount same, date changed
        LocalDate newDate = LocalDate.now().plusDays(15);
        Money newFee = new Money("45.00");

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(existingTransaction));
        when(feeConfigurationRepository.findBestMatch(any(), anyLong()))
                .thenReturn(Optional.of(feeConfiguration));
        when(transactionRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        Transaction result = updateTransactionUseCase.execute(
                new UpdateTransactionUseCase.UpdateTransactionCommand(
                        1L,
                        Optional.of(existingTransaction.getSourceAccount().getValue()),
                        Optional.of(existingTransaction.getDestinationAccount().getValue()),
                        Optional.of(existingTransaction.getTransferAmount().getAmount()), // Same
                        Optional.of(newDate) // Changed
        ));

        // Then
        assertThat(result.getScheduledDate()).isEqualTo(newDate);
        assertThat(result.getTransferFee()).isEqualTo(newFee);
    }
}
