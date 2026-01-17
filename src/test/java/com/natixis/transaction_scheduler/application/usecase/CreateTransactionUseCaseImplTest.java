package com.natixis.transaction_scheduler.application.usecase;

import com.natixis.transaction_scheduler.domain.exception.FeeConfigurationNotFoundException;
import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import com.natixis.transaction_scheduler.domain.port.in.CreateTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.out.FeeConfigurationRepository;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateTransactionUseCase Tests")
class CreateTransactionUseCaseImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FeeConfigurationRepository feeConfigurationRepository;

    @InjectMocks
    private CreateTransactionUseCaseImpl createTransactionUseCase;

    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal transferAmount;
    private LocalDate scheduledDate;
    private BigDecimal calculatedFee;
    private FeeConfiguration feeConfiguration;

    @BeforeEach
    void setUp() {
        sourceAccount = "PT50 0002 0123 1234 5678 9015 4";
        destinationAccount = "DE89 3704 0044 0532 0130 00";
        transferAmount = BigDecimal.valueOf(1500.00);
        scheduledDate = LocalDate.now().plusDays(5);
        calculatedFee = BigDecimal.valueOf(135.00);

        feeConfiguration = FeeConfiguration.builder()
                .id(1L)
                .feeType("TAXA_B")
                .minAmount(new Money("1000.01"))
                .maxAmount(new Money("2000.00"))
                .minDays(1)
                .maxDays(10)
                .percentageFee(new BigDecimal("0.09"))
                .fixedFee(null)
                .priority(2)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should create transaction successfully with fee calculation")
    void shouldCreateTransactionSuccessfully() {
        // Given
        when(feeConfigurationRepository.findBestMatch(any(Money.class), anyLong()))
                .thenReturn(Optional.of(feeConfiguration));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction tx = invocation.getArgument(0);
                    return Transaction.builder()
                            .id(1L)
                            .sourceAccount(tx.getSourceAccount())
                            .destinationAccount(tx.getDestinationAccount())
                            .transferAmount(tx.getTransferAmount())
                            .transferFee(tx.getTransferFee())
                            .feeConfiguration(tx.getFeeConfiguration())
                            .scheduledDate(tx.getScheduledDate())
                            .createdAt(tx.getCreatedAt())
                            .updatedAt(tx.getUpdatedAt())
                            .build();
                });

        // When
        Transaction result = createTransactionUseCase.execute(
                new CreateTransactionUseCase.CreateTransactionCommand(
                    sourceAccount,
                    destinationAccount,
                    transferAmount,
                    scheduledDate
        ));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSourceAccount().getFormattedValue()).isEqualTo(sourceAccount);
        assertThat(result.getDestinationAccount().getFormattedValue()).isEqualTo(destinationAccount);
        assertThat(result.getTransferAmount().getAmount()).isEqualTo(transferAmount.setScale(2, RoundingMode.HALF_UP));
        assertThat(result.getTransferFee().getAmount()).isEqualTo(calculatedFee.setScale(2, RoundingMode.HALF_UP));
        assertThat(result.getFeeConfiguration()).isEqualTo(feeConfiguration);

        // Verify interactions
        verify(feeConfigurationRepository).findBestMatch(any(Money.class), anyLong());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when no fee configuration found")
    void shouldThrowExceptionWhenNoFeeConfigurationFound() {
        // Given
        when(feeConfigurationRepository.findBestMatch(any(), anyLong()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> createTransactionUseCase.execute(
                new CreateTransactionUseCase.CreateTransactionCommand(
                        sourceAccount,
                        destinationAccount,
                        transferAmount,
                        scheduledDate
                    )))
                .isInstanceOf(FeeConfigurationNotFoundException.class)
                .hasMessageContaining("No fee configuration found");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should calculate correct fee for Taxa B scenario")
    void shouldCalculateCorrectFeeForTaxaB() {
        // Given - Taxa B: 1001-2000 EUR, 1-10 days, 9%
        final var amount = BigDecimal.valueOf(1500.00);
        final var expectedFee = new Money("135.00"); // 1500 * 0.09 = 135

        when(feeConfigurationRepository.findBestMatch(any(), anyLong()))
                .thenReturn(Optional.of(feeConfiguration));
        when(transactionRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        Transaction result = createTransactionUseCase.execute(
                new CreateTransactionUseCase.CreateTransactionCommand(
                        sourceAccount,
                        destinationAccount,
                        amount,
                        scheduledDate
                ));

        // Then
        assertThat(result.getTransferFee()).isEqualTo(expectedFee);
        assertThat(result.getTotalAmount().getAmount())
                .isEqualByComparingTo("1635.00"); // 1500 + 135
    }

    @Test
    @DisplayName("Should save transaction with all required fields")
    void shouldSaveTransactionWithAllFields() {
        // Given
        when(feeConfigurationRepository.findBestMatch(any(), anyLong()))
                .thenReturn(Optional.of(feeConfiguration));

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(transactionCaptor.capture()))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        createTransactionUseCase.execute(
                new CreateTransactionUseCase.CreateTransactionCommand(
                        sourceAccount,
                        destinationAccount,
                        transferAmount,
                        scheduledDate
                ));

        // Then
        Transaction saved = transactionCaptor.getValue();
        assertThat(saved.getSourceAccount().getFormattedValue()).isEqualTo(sourceAccount);
        assertThat(saved.getDestinationAccount().getFormattedValue()).isEqualTo(destinationAccount);
        assertThat(saved.getTransferAmount().getAmount()).isEqualTo(transferAmount.setScale(2, RoundingMode.HALF_UP));
        assertThat(saved.getTransferFee().getAmount()).isEqualTo(calculatedFee.setScale(2, RoundingMode.HALF_UP));
        assertThat(saved.getScheduledDate()).isEqualTo(scheduledDate);
        assertThat(saved.getFeeConfiguration()).isEqualTo(feeConfiguration);
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
