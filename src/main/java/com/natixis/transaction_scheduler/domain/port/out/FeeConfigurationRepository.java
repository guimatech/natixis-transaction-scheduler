package com.natixis.transaction_scheduler.domain.port.out;

import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;

import java.util.Optional;

/**
 * Output Port for fee calculation.
 */
public interface FeeConfigurationRepository {
    Optional<FeeConfiguration> findBestMatch(Money transferAmount, Long days);
}
