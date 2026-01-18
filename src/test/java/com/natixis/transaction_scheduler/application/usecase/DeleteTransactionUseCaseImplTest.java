package com.natixis.transaction_scheduler.application.usecase;

import com.natixis.transaction_scheduler.domain.exception.ResourceNotFoundException;
import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteTransactionUseCase Tests")
class DeleteTransactionUseCaseImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DeleteTransactionUseCaseImpl deleteTransactionUseCase;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .id(1L)
                .sourceAccount(new AccountNumber("PT50 0002 0123 1234 5678 9015 4"))
                .destinationAccount(new AccountNumber("DE89 3704 0044 0532 0130 00"))
                .transferAmount(new Money("500.00"))
                .transferFee(new Money("18.00"))
                .scheduledDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should delete transaction successfully")
    void shouldDeleteTransactionSuccessfully() {
        // Given
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));
        doNothing().when(transactionRepository).delete(transaction);

        // When
        deleteTransactionUseCase.execute(1L);

        // Then
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).delete(transaction);
    }

    @Test
    @DisplayName("Should throw exception when transaction not found")
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Given
        when(transactionRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deleteTransactionUseCase.execute(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cannot delete. Transaction not found with ID: 999");

        verify(transactionRepository).findById(999L);
        verify(transactionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should verify transaction exists before deleting")
    void shouldVerifyTransactionExistsBeforeDeleting() {
        // Given
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        // When
        deleteTransactionUseCase.execute(1L);

        // Then
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).delete(transaction);
    }
}
