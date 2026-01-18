package com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.controller;

import com.natixis.transaction_scheduler.domain.exception.ResourceNotFoundException;
import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import com.natixis.transaction_scheduler.domain.port.out.FeeConfigurationRepository;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for TransactionController.
 * Tests full stack with IBAN account numbers.
 *<p>
 * Real IBAN examples used:
 * - Portugal: PT50000201231234567890154 (25 chars)
 * - Germany: DE89370400440532013000 (22 chars)
 * - France: FR7630006000011234567890189 (27 chars)
 * - Spain: ES9121000418450200051332 (24 chars)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("TransactionController Integration Tests - IBAN Format")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FeeConfigurationRepository feeConfigurationRepository;

    // Real IBAN test accounts
    private static final String IBAN_PORTUGAL = "PT50000201231234567890154";
    private static final String IBAN_GERMANY = "DE89370400440532013000";
    private static final String IBAN_FRANCE = "FR7630006000011234567890189";
    private static final String IBAN_SPAIN = "ES9121000418450200051332";

    // ========================================
    // POST /v1/transactions - Create
    // ========================================

    @Test
    @DisplayName("POST - Should create transaction with IBAN accounts and Taxa A")
    void shouldCreateTransactionWithIbanAndTaxaA() throws Exception {
        LocalDate today = LocalDate.now();

        String requestJson = """
                {
                  "sourceAccount": "PT50000201231234567890154",
                  "destinationAccount": "DE89370400440532013000",
                  "transferAmount": 500,
                  "scheduledDate": "%s"
                }
                """.formatted(today);

        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.sourceAccount").value("PT50000201231234567890154"))
                .andExpect(jsonPath("$.destinationAccount").value("DE89370400440532013000"))
                .andExpect(jsonPath("$.transferAmount").value(500.00))
                .andExpect(jsonPath("$.transferFee").value(18.00)) // 500 * 0.030 + 3 = 18.00
                .andExpect(jsonPath("$.totalAmount").value(518.00))
                .andExpect(jsonPath("$.scheduledDate").value(today.toString()));
    }

    @Test
    @DisplayName("POST - Should create transaction with Taxa B (1001-2000 EUR, 1-10 days)")
    void shouldCreateTransactionWithTaxaB() throws Exception {
        LocalDate day5DaysFuture = LocalDate.now().plusDays(5);
        // Given - Amount 1500, 5 days = Taxa B (9%)
        String requestJson = """
                {
                  "sourceAccount": "FR7630006000011234567890189",
                  "destinationAccount": "ES9121000418450200051332",
                  "transferAmount": 1500,
                  "scheduledDate": "%s"
                }
                """.formatted(day5DaysFuture);

        // When & Then
        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceAccount").value("FR7630006000011234567890189"))
                .andExpect(jsonPath("$.destinationAccount").value("ES9121000418450200051332"))
                .andExpect(jsonPath("$.transferAmount").value(1500.00))
                .andExpect(jsonPath("$.transferFee").value(135.00)) // 1500 * 0.09 = 135
                .andExpect(jsonPath("$.totalAmount").value(1635.00));
    }

    @Test
    @DisplayName("POST - Should create transaction with Taxa C (>2000 EUR, 11-20 days)")
    void shouldCreateTransactionWithTaxaC() throws Exception {
        LocalDate day15Future = LocalDate.now().plusDays(15);
        // Given - Amount 3500, 15 days = Taxa C (8.2%)
        String requestJson = """
                {
                  "sourceAccount": "PT50000201231234567890154",
                  "destinationAccount": "DE89370400440532013000",
                  "transferAmount": 3500,
                  "scheduledDate": "%s"
                }
                """.formatted(day15Future);

        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transferAmount").value(3500.00))
                .andExpect(jsonPath("$.transferFee").value(287.00)) // 3500 * 0.082 = 287
                .andExpect(jsonPath("$.totalAmount").value(3787.00));
    }

    @Test
    @DisplayName("POST - Should return 400 for invalid IBAN format")
    void shouldReturn400ForInvalidIban() throws Exception {
        LocalDate today = LocalDate.now();
        // Given - Invalid IBAN (too short)
        String requestJson = """
                {
                  "sourceAccount": "PT1234",
                  "destinationAccount": "DE89370400440532013000",
                  "transferAmount": 500,
                  "scheduledDate": "%s"
                }
                """.formatted(today);

        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid request parameters")))
                .andExpect(jsonPath("$.details",
                        hasItem(containsString("Invalid source account format"))));
    }

    @Test
    @DisplayName("POST - Should return 400 for IBAN without country code")
    void shouldReturn400ForIbanWithoutCountryCode() throws Exception {
        LocalDate today = LocalDate.now();
        // Given - Missing country code
        String requestJson = """
                {
                  "sourceAccount": "50000201231234567890154",
                  "destinationAccount": "DE89370400440532013000",
                  "transferAmount": 500,
                  "scheduledDate": "%s"
                }
                """.formatted(today);

        // When & Then
        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid request parameters")))
                .andExpect(jsonPath("$.details", hasItem(containsString("Invalid source account format"))));
    }

    @Test
    @DisplayName("POST - Should return 400 for same source and destination accounts")
    void shouldReturn400ForSameSourceAndDestination() throws Exception {
        LocalDate today = LocalDate.now();
        // Given - Same IBAN
        String requestJson = """
                {
                  "sourceAccount": "PT50000201231234567890154",
                  "destinationAccount": "PT50000201231234567890154",
                  "transferAmount": 500,
                  "scheduledDate": "%s"
                }
                """.formatted(today);

        // When & Then
        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        containsString("Source and destination accounts cannot be the same")));
    }

    @Test
    @DisplayName("POST - Should return 400 for past scheduled date")
    void shouldReturn400ForPastDate() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        // Given
        String requestJson = """
                {
                  "sourceAccount": "PT50000201231234567890154",
                  "destinationAccount": "DE89370400440532013000",
                  "transferAmount": 500,
                  "scheduledDate": "%s"
                }
                """.formatted(yesterday);

        // When & Then
        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        containsString("Invalid request parameters")))
                .andExpect(jsonPath("$.details",
                        hasItem(containsString("Scheduled date must be today or in the future"))));
    }

    @Test
    @DisplayName("POST - Should return 400 for zero amount")
    void shouldReturn400ForZeroAmount() throws Exception {
        LocalDate today = LocalDate.now();
        // Given
        String requestJson = """
                {
                  "sourceAccount": "PT50000201231234567890154",
                  "destinationAccount": "DE89370400440532013000",
                  "transferAmount": 0,
                  "scheduledDate": "%s"
                }
                """.formatted(today);

        // When & Then
        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid request parameters")))
                .andExpect(jsonPath("$.details",
                        hasItem(containsString("Amount must be greater than zero"))));
    }

    @Test
    @DisplayName("POST - Should return 400 for negative amount")
    void shouldReturn400ForNegativeAmount() throws Exception {
        LocalDate today = LocalDate.now();
        // Given
        String requestJson = """
                {
                  "sourceAccount": "PT50000201231234567890154",
                  "destinationAccount": "DE89370400440532013000",
                  "transferAmount": -100,
                  "scheduledDate": "%s"
                }
                """.formatted(today);

        // When & Then
        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid request parameters")))
                .andExpect(jsonPath("$.details",
                        hasItem(containsString("Amount must be greater than zero"))));
    }

    // ========================================
    // GET /v1/transactions/{id}
    // ========================================

    @Test
    @DisplayName("GET /{id} - Should get transaction by ID")
    void shouldGetTransactionById() throws Exception {
        // Given
        Transaction saved = createAndSaveTransaction(
                IBAN_PORTUGAL,
                IBAN_GERMANY,
                "500.00",
                LocalDate.now()
        );

        // When & Then
        mockMvc.perform(get("/v1/transactions/{id}", saved.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.sourceAccount").value(IBAN_PORTUGAL))
                .andExpect(jsonPath("$.destinationAccount").value(IBAN_GERMANY))
                .andExpect(jsonPath("$.transferAmount").value(500.00));
    }

    @Test
    @DisplayName("GET /{id} - Should return 404 for non-existent ID")
    void shouldReturn404ForNonExistentId() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/transactions/{id}", 99999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    // ========================================
    // GET /v1/transactions - Get All
    // ========================================

    @Test
    @DisplayName("GET / - Should get all transactions")
    void shouldGetAllTransactions() throws Exception {
        // Given
        createAndSaveTransaction(IBAN_PORTUGAL, IBAN_GERMANY, "500.00", LocalDate.now());
        createAndSaveTransaction(IBAN_FRANCE, IBAN_SPAIN, "800.00", LocalDate.now());
        createAndSaveTransaction(IBAN_GERMANY, IBAN_FRANCE, "1200.00", LocalDate.now().plusDays(5));

        // When & Then
        mockMvc.perform(get("/v1/transactions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].sourceAccount").exists())
                .andExpect(jsonPath("$[1].sourceAccount").exists())
                .andExpect(jsonPath("$[2].sourceAccount").exists());
    }

    @Test
    @DisplayName("GET / - Should return empty list when no transactions")
    void shouldReturnEmptyListWhenNoTransactions() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/transactions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========================================
    // GET /v1/transactions/scheduled/{date}
    // ========================================

    @Test
    @DisplayName("GET /scheduled/{date} - Should get transactions by date")
    void shouldGetTransactionsByScheduledDate() throws Exception {
        // Given
        LocalDate targetDate = LocalDate.now().plusDays(5);
        createAndSaveTransaction(IBAN_PORTUGAL, IBAN_GERMANY, "1500.00", targetDate);
        createAndSaveTransaction(IBAN_FRANCE, IBAN_SPAIN, "1800.00", targetDate);
        createAndSaveTransaction(IBAN_GERMANY, IBAN_FRANCE, "200.00", LocalDate.now());

        // When & Then
        mockMvc.perform(get("/v1/transactions/scheduled/{date}", targetDate))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].scheduledDate").value(targetDate.toString()))
                .andExpect(jsonPath("$[1].scheduledDate").value(targetDate.toString()));
    }

    @Test
    @DisplayName("GET /scheduled/{date} - Should return empty for date with no transactions")
    void shouldReturnEmptyForDateWithNoTransactions() throws Exception {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(100);

        // When & Then
        mockMvc.perform(get("/v1/transactions/scheduled/{date}", futureDate))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========================================
    // GET /v1/transactions/accounts/{accountNumber}
    // ========================================

    @Test
    @DisplayName("GET /accounts/{accountNumber} - Should get transactions by source account")
    void shouldGetTransactionsBySourceAccount() throws Exception {
        // Given
        createAndSaveTransaction(IBAN_PORTUGAL, IBAN_GERMANY, "500.00", LocalDate.now());
        createAndSaveTransaction(IBAN_PORTUGAL, IBAN_FRANCE, "800.00", LocalDate.now());
        createAndSaveTransaction(IBAN_FRANCE, IBAN_SPAIN, "1200.00", LocalDate.now().plusDays(2));

        // When & Then
        mockMvc.perform(get("/v1/transactions/accounts/{accountNumber}", IBAN_PORTUGAL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sourceAccount").value(IBAN_PORTUGAL))
                .andExpect(jsonPath("$[1].sourceAccount").value(IBAN_PORTUGAL));
    }

    // ========================================
    // PUT /v1/transactions/{id} - Full Update
    // ========================================

    @Test
    @DisplayName("PUT /{id} - Should update transaction completely with new IBANs")
    void shouldUpdateTransactionCompletelyWithNewIbans() throws Exception {
        // Given
        Transaction existing = createAndSaveTransaction(
                IBAN_PORTUGAL,
                IBAN_GERMANY,
                "500.00",
                LocalDate.now()
        );
        LocalDate day5DaysFuture = LocalDate.now().plusDays(5);

        String updateJson = """
                {
                  "sourceAccount": "FR7630006000011234567890189",
                  "destinationAccount": "ES9121000418450200051332",
                  "transferAmount": 1500,
                  "scheduledDate": "%s"
                }
                """.formatted(day5DaysFuture);

        // When & Then
        mockMvc.perform(put("/v1/transactions/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existing.getId()))
                .andExpect(jsonPath("$.sourceAccount").value(IBAN_FRANCE))
                .andExpect(jsonPath("$.destinationAccount").value(IBAN_SPAIN))
                .andExpect(jsonPath("$.transferAmount").value(1500.00))
                .andExpect(jsonPath("$.transferFee").value(135.00)); // Taxa B: 9%
    }

    @Test
    @DisplayName("PUT /{id} - Should return 404 for non-existent transaction")
    void shouldReturn404WhenUpdatingNonExistent() throws Exception {
        LocalDate today = LocalDate.now();
        // Given
        String updateJson = """
                {
                  "sourceAccount": "PT50000201231234567890154",
                  "destinationAccount": "DE89370400440532013000",
                  "transferAmount": 500,
                  "scheduledDate": "%s"
                }
                """.formatted(today);

        // When & Then
        mockMvc.perform(put("/v1/transactions/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ========================================
    // PATCH /v1/transactions/{id} - Partial
    // ========================================

    @Test
    @DisplayName("PATCH /{id} - Should update only amount")
    void shouldPatchOnlyAmount() throws Exception {
        // Given
        Transaction existing = createAndSaveTransaction(
                IBAN_PORTUGAL,
                IBAN_GERMANY,
                "500.00",
                LocalDate.now()
        );

        String patchJson = """
                {
                  "transferAmount": 750
                }
                """;

        // When & Then
        mockMvc.perform(patch("/v1/transactions/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceAccount").value(IBAN_PORTUGAL)) // Unchanged
                .andExpect(jsonPath("$.destinationAccount").value(IBAN_GERMANY)) // Unchanged
                .andExpect(jsonPath("$.transferAmount").value(750.00)) // Changed
                .andExpect(jsonPath("$.transferFee").value(25.5)); // Recalculated
    }

    @Test
    @DisplayName("PATCH /{id} - Should update only accounts (no fee recalc)")
    void shouldPatchOnlyAccounts() throws Exception {
        // Given
        Transaction existing = createAndSaveTransaction(
                IBAN_PORTUGAL,
                IBAN_GERMANY,
                "500.00",
                LocalDate.now()
        );
        BigDecimal originalFee = existing.getTransferFee().getAmount();

        String patchJson = """
                {
                  "sourceAccount": "FR7630006000011234567890189",
                  "destinationAccount": "ES9121000418450200051332"
                }
                """;

        // When & Then
        mockMvc.perform(patch("/v1/transactions/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceAccount").value(IBAN_FRANCE))
                .andExpect(jsonPath("$.destinationAccount").value(IBAN_SPAIN))
                .andExpect(jsonPath("$.transferAmount").value(500.00)) // Unchanged
                .andExpect(jsonPath("$.transferFee").value(originalFee.setScale(1, RoundingMode.HALF_UP))); // Not recalculated
    }

    @Test
    @DisplayName("PATCH /{id} - Should update only date (triggers fee recalc)")
    void shouldPatchOnlyDate() throws Exception {
        // Given
        Transaction existing = createAndSaveTransaction(
                IBAN_PORTUGAL,
                IBAN_GERMANY,
                "1500.00",
                LocalDate.now().plusDays(2)
        );
        LocalDate day5Future = LocalDate.now().plusDays(5);

        String patchJson = """
                {
                  "scheduledDate": "%s"
                }
                """.formatted(day5Future);

        // When & Then
        mockMvc.perform(patch("/v1/transactions/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduledDate").value(day5Future.toString()))
                .andExpect(jsonPath("$.transferFee").value(135.00)); // Taxa B
    }

    // ========================================
    // DELETE /v1/transactions/{id}
    // ========================================

    @Test
    @DisplayName("DELETE /{id} - Should delete transaction")
    void shouldDeleteTransaction() throws Exception {
        // Given
        Transaction existing = createAndSaveTransaction(
                IBAN_PORTUGAL,
                IBAN_GERMANY,
                "500.00",
                LocalDate.now()
        );

        // When & Then - Delete
        mockMvc.perform(delete("/v1/transactions/{id}", existing.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/v1/transactions/{id}", existing.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /{id} - Should return 404 for non-existent transaction")
    void shouldReturn404WhenDeletingNonExistent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/transactions/{id}", 99999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ========================================
    // Helper Methods
    // ========================================

    private Transaction createAndSaveTransaction(
            String sourceIban,
            String destinationIban,
            String amount,
            LocalDate scheduledDate) {

        Transaction transaction = Transaction.create(
                new AccountNumber(sourceIban),
                new AccountNumber(destinationIban),
                new Money(amount),
                scheduledDate,
                calculateFee(new Money(amount), scheduledDate),
                findFeeConfig(new Money(amount), scheduledDate)
        );

        return transactionRepository.save(transaction);
    }

    private Money calculateFee(Money amount, LocalDate scheduledDate) {
        FeeConfiguration config = findFeeConfig(amount, scheduledDate);
        return config.calculateFee(amount);
    }

    private FeeConfiguration findFeeConfig(Money amount, LocalDate scheduledDate) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), scheduledDate);
        return feeConfigurationRepository.findBestMatch(amount, days)
                .orElseThrow(() -> new ResourceNotFoundException("No fee config found"));
    }

    /**
     * Fee A (0–1000) with a future scheduled date has no defined rule (???),
     * so the API must return 404 - fee configuration not found.
     */
    @Test
    @DisplayName("POST - Should return 404 when Fee A amount with future date (no fee rule)")
    void shouldReturn404WhenTaxaAWithFutureDate() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(5);

        String requestJson = """
            {
              "sourceAccount": "PT50000201231234567890154",
              "destinationAccount": "DE89370400440532013000",
              "transferAmount": 500,
              "scheduledDate": "%s"
            }
            """.formatted(futureDate);

        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        containsString("No fee configuration found")));
    }

    /**
     * Fee B (1001–2000) with the scheduled date equal to today has no defined rule (???),
     * so the API must return 404.
     */
    @Test
    @DisplayName("POST - Should return 404 when Fee B amount with same-day scheduling (no fee rule)")
    void shouldReturn404WhenTaxaBWithSameDay() throws Exception {
        LocalDate today = LocalDate.now();

        String requestJson = """
            {
              "sourceAccount": "FR7630006000011234567890189",
              "destinationAccount": "ES9121000418450200051332",
              "transferAmount": 1500,
              "scheduledDate": "%s"
            }
            """.formatted(today);

        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        containsString("No fee configuration found")));
    }

    /**
     * Fee B (1001–2000) with a scheduled date more than 10 days in the future has no defined rule (???),
     * so the API must return 404.
     */
    @Test
    @DisplayName("POST - Should return 404 when Fee B amount with more than 10 days (no fee rule)")
    void shouldReturn404WhenTaxaBWithMoreThan10Days() throws Exception {
        LocalDate future15 = LocalDate.now().plusDays(15);

        String requestJson = """
            {
              "sourceAccount": "FR7630006000011234567890189",
              "destinationAccount": "ES9121000418450200051332",
              "transferAmount": 1500,
              "scheduledDate": "%s"
            }
            """.formatted(future15);

        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        containsString("No fee configuration found")));
    }

    /**
     * Fee C (>2000) with a scheduled date between 1 and 10 days from today has no defined rule (???),
     * so the API must return 404.
     */
    @Test
    @DisplayName("POST - Should return 404 when Fee C amount with 1-10 days (no fee rule)")
    void shouldReturn404WhenTaxaCWith1To10Days() throws Exception {
        LocalDate future5 = LocalDate.now().plusDays(5);

        String requestJson = """
            {
              "sourceAccount": "PT50000201231234567890154",
              "destinationAccount": "DE89370400440532013000",
              "transferAmount": 3000,
              "scheduledDate": "%s"
            }
            """.formatted(future5);

        mockMvc.perform(post("/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        containsString("No fee configuration found")));
    }
}
