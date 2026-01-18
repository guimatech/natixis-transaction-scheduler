package com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.repository;

import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import com.natixis.transaction_scheduler.domain.port.out.FeeConfigurationRepository;
import com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.entity.FeeConfigurationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FeeConfigurationRepositoryAdapter implements FeeConfigurationRepository {

    private final JpaFeeConfigurationRepository jpaFeeConfigurationRepository;

    @Override
    public Optional<FeeConfiguration> findBestMatch(Money transferAmount, Long days) {
        return this.jpaFeeConfigurationRepository
                .findBestMatch(transferAmount.getAmount(), days).stream()
                .findFirst().flatMap(feeConfiguration ->
                        Optional.ofNullable(feeConfiguration.toFeeConfiguration()));
    }
}
