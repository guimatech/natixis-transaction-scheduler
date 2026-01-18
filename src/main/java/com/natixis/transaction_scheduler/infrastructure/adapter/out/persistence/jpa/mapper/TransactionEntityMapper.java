package com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.mapper;

import com.natixis.transaction_scheduler.domain.model.Transaction;
import com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for Transaction domain model to/from entities.
 * Supports Java Records seamlessly.
 * <p>
 * MapStruct generates implementation at compile-time.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionEntityMapper extends UtilsMapper {

    TransactionEntityMapper INSTANCE = Mappers.getMapper(TransactionEntityMapper.class);

    /**
     * Map Transaction domain model to entity.
     * MapStruct handles record constructor automatically.
     */
    @Mapping(target = "sourceAccount", source = "sourceAccount.value")
    @Mapping(target = "destinationAccount", source = "destinationAccount.value")
    @Mapping(target = "transferAmount", source = "transferAmount.amount")
    @Mapping(target = "transferFee", source = "transferFee.amount")
    TransactionEntity toEntity(Transaction transaction);

    @Mapping(target = "sourceAccount", source = "sourceAccount", qualifiedByName = "toAccountNumber")
    @Mapping(target = "destinationAccount", source = "destinationAccount", qualifiedByName = "toAccountNumber")
    @Mapping(target = "transferAmount", source = "transferAmount", qualifiedByName = "toMoney")
    @Mapping(target = "transferFee", source = "transferFee", qualifiedByName = "toMoney")
    Transaction toModel(TransactionEntity transactionEntity);
}
