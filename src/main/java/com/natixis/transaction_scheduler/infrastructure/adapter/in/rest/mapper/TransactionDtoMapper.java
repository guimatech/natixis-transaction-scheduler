package com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.mapper;

import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.domain.port.in.CreateTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.in.UpdateTransactionUseCase;
import com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.dto.request.TransactionPatchRequest;
import com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.dto.request.TransactionRequest;
import com.natixis.transaction_scheduler.infrastructure.adapter.in.rest.dto.response.TransactionResponse;
import com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.mapper.UtilsMapper;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for Transaction domain model to/from DTOs.
 * Supports Java Records seamlessly.
 * <p>
 * MapStruct generates implementation at compile-time.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionDtoMapper extends UtilsMapper {

    TransactionDtoMapper INSTANCE = Mappers.getMapper(TransactionDtoMapper.class);

    /**
     * Map Transaction domain model to response DTO (record).
     * MapStruct handles record constructor automatically.
     */
    @Mapping(target = "sourceAccount", source = "sourceAccount.value")
    @Mapping(target = "destinationAccount", source = "destinationAccount.value")
    @Mapping(target = "transferAmount", source = "transferAmount.amount")
    @Mapping(target = "transferFee", source = "transferFee.amount")
    @Mapping(target = "totalAmount", expression = "java(transaction.getTotalAmount().getAmount())")
    TransactionResponse toResponse(Transaction transaction);

    CreateTransactionUseCase.CreateTransactionCommand toCreateCommand(@Valid TransactionRequest request);

    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "request.sourceAccount", target = "sourceAccount", qualifiedByName = "wrapAsOptional")
    @Mapping(source = "request.destinationAccount", target = "destinationAccount", qualifiedByName = "wrapAsOptional")
    @Mapping(source = "request.transferAmount", target = "transferAmount", qualifiedByName = "wrapAsOptional")
    @Mapping(source = "request.scheduledDate", target = "scheduledDate", qualifiedByName = "wrapAsOptional")
    UpdateTransactionUseCase.UpdateTransactionCommand toUpdateCommand(Long id, @Valid TransactionRequest request);

    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "request.sourceAccount", target = "sourceAccount", qualifiedByName = "wrapAsOptional")
    @Mapping(source = "request.destinationAccount", target = "destinationAccount", qualifiedByName = "wrapAsOptional")
    @Mapping(source = "request.transferAmount", target = "transferAmount", qualifiedByName = "wrapAsOptional")
    @Mapping(source = "request.scheduledDate", target = "scheduledDate", qualifiedByName = "wrapAsOptional")
    UpdateTransactionUseCase.UpdateTransactionCommand toUpdateCommand(Long id, @Valid TransactionPatchRequest request);
}
