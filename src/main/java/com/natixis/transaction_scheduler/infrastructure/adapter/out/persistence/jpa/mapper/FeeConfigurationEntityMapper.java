package com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.mapper;

import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.entity.FeeConfigurationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Qualifier;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for Transaction domain model to/from entities.
 * Supports Java Records seamlessly.
 * <p>
 * MapStruct generates implementation at compile-time.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FeeConfigurationEntityMapper extends UtilsMapper {

    FeeConfigurationEntityMapper INSTANCE = Mappers.getMapper(FeeConfigurationEntityMapper.class);

    /**
     * Map FeeConfiguration domain entity to model.
     * MapStruct handles record constructor automatically.
     */
    @Mapping(target = "minAmount", source = "minAmount", qualifiedByName = "toMoney")
    @Mapping(target = "maxAmount", source = "maxAmount", qualifiedByName = "toMoney")
    @Mapping(target = "fixedFee", source = "fixedFee", qualifiedByName = "toMoney")
    FeeConfiguration toModel(FeeConfigurationEntity feeConfigurationEntity);
}
