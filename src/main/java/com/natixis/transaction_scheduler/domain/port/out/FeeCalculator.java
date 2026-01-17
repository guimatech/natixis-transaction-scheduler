package com.natixis.transaction_scheduler.domain.port.out;

import com.natixis.transaction_scheduler.domain.model.FeeConfiguration;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import java.time.LocalDate;

/**
 * Output Port for fee calculation.
 */
public interface FeeCalculator {
    Money calculate(Money transferAmount, LocalDate scheduledDate);
    FeeConfiguration determineFeeConfiguration(Money transferAmount);
}
