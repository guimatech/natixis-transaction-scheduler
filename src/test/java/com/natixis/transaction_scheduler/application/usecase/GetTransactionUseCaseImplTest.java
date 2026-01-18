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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetTransactionUseCase Tests")
class GetTransactionUseCaseImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GetTransactionUseCaseImpl getTransactionUseCase;

    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    void setUp() {
        transaction1 = Transaction.builder()
                .id(1L)
                .sourceAccount(new AccountNumber("FR76 3000 6000 0112 3456 7890 189"))
                .destinationAccount(new AccountNumber("PT50 0002 0123 1234 5678 9015 4"))
                .transferAmount(new Money("500.00"))
                .transferFee(new Money("18.00"))
                .scheduledDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        transaction2 = Transaction.builder()
                .id(2L)
                .sourceAccount(new AccountNumber("FR76 3000 6000 0112 3456 7890 189"))
                .destinationAccount(new AccountNumber("DE89 3704 0044 0532 0130 00"))
                .transferAmount(new Money("1500.00"))
                .transferFee(new Money("135.00"))
                .scheduledDate(LocalDate.now().plusDays(5))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get transaction by ID successfully")
    void shouldGetTransactionById() {
        // Given
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction1));

        // When
        Transaction result = getTransactionUseCase.getById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSourceAccount().getFormattedValue()).isEqualTo("FR76 3000 6000 0112 3456 7890 189");

        verify(transactionRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when transaction not found by ID")
    void shouldThrowExceptionWhenNotFoundById() {
        // Given
        when(transactionRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> getTransactionUseCase.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with ID: 999");

        verify(transactionRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get all transactions")
    void shouldGetAllTransactions() {
        // Given
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
        when(transactionRepository.findAll())
                .thenReturn(transactions);

        // When
        List<Transaction> result = getTransactionUseCase.getAll();

        // Then
        assertThat(result).hasSize(2)
                .containsExactly(transaction1, transaction2);

        verify(transactionRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no transactions exist")
    void shouldReturnEmptyListWhenNoTransactions() {
        // Given
        when(transactionRepository.findAll())
                .thenReturn(List.of());

        // When
        List<Transaction> result = getTransactionUseCase.getAll();

        // Then
        assertThat(result).isEmpty();

        verify(transactionRepository).findAll();
    }

    @Test
    @DisplayName("Should get transactions by scheduled date")
    void shouldGetTransactionsByScheduledDate() {
        // Given
        LocalDate date = LocalDate.now();
        when(transactionRepository.findByScheduledDate(date))
                .thenReturn(List.of(transaction1));

        // When
        List<Transaction> result = getTransactionUseCase.getByScheduledDate(date);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(transaction1);

        verify(transactionRepository).findByScheduledDate(date);
    }

    @Test
    @DisplayName("Should get transactions by source account")
    void shouldGetTransactionsBySourceAccount() {
        // Given
        String accountNumber = "FR76 3000 6000 0112 3456 7890 189";
        when(transactionRepository.findBySourceAccount(accountNumber))
                .thenReturn(Arrays.asList(transaction1, transaction2));

        // When
        List<Transaction> result = getTransactionUseCase.getBySourceAccount(accountNumber);

        // Then
        assertThat(result).hasSize(2)
                .containsExactly(transaction1, transaction2);

        verify(transactionRepository).findBySourceAccount(accountNumber);
    }
}
