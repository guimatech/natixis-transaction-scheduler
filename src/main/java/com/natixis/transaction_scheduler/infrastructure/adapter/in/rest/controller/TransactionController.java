package com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.controller;

import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.port.in.CreateTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.in.DeleteTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.in.GetTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.in.UpdateTransactionUseCase;
import com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.dto.request.TransactionRequest;
import com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.dto.response.TransactionResponse;
import com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.mapper.TransactionDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction management APIs")
public class TransactionController {
    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetTransactionUseCase getTransactionUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;

    @Operation(
            summary = "Create a new transaction",
            description = "Creates a new scheduled transaction with automatic fee calculation based on amount and scheduled date"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction created successfully",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Transaction details to create",
                    required = true
            )
            @Valid @RequestBody TransactionRequest request) {

        log.info("REST: Received request to create transaction");

        CreateTransactionUseCase.CreateTransactionCommand command =
                TransactionDtoMapper.INSTANCE.toCreateCommand(request);

        Transaction transaction = createTransactionUseCase.execute(command);
        TransactionResponse response = TransactionDtoMapper.INSTANCE.toResponse(transaction);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get transaction by ID",
            description = "Retrieves a specific transaction by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        log.info("REST: Getting transaction with ID: {}", id);

        Transaction transaction = getTransactionUseCase.getById(id);
        TransactionResponse response = TransactionDtoMapper.INSTANCE.toResponse(transaction);

            return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all transactions",
            description = "Retrieves a list of all transactions in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of transactions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        log.info("REST: Getting all transactions");

        List<Transaction> transactions = getTransactionUseCase.getAll();
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionDtoMapper.INSTANCE::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "Update an existing transaction",
            description = "Updates a transaction and recalculates the fee based on new values"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction updated successfully",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        log.info("REST: Updating transaction with ID: {}", id);

        UpdateTransactionUseCase.UpdateTransactionCommand command =
                TransactionDtoMapper.INSTANCE.toUpdateCommand(id, request);

        Transaction transaction = updateTransactionUseCase.execute(command);
        TransactionResponse response = TransactionDtoMapper.INSTANCE.toResponse(transaction);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a transaction",
            description = "Permanently deletes a transaction from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Transaction deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        log.info("REST: Deleting transaction with ID: {}", id);
        deleteTransactionUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
