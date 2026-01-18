package com.natixis.transaction_scheduler.infrastructure.adapter.out.persistence.jpa.mapper;

import com.natixis.transaction_scheduler.domain.model.valueobject.AccountNumber;
import com.natixis.transaction_scheduler.domain.model.valueobject.Money;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.Optional;

public interface UtilsMapper {
    @Named("toMoney")
    default Money toMoney(BigDecimal amount) {
        if (amount == null) {
            return null;
        }

        return new Money(amount);
    }

    @Named("toAccountNumber")
    default AccountNumber toAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }

        return new AccountNumber(accountNumber);
    }

    @Named("wrapAsOptional")
    default <T> Optional<T> wrapAsOptional(T data) {
        return Optional.ofNullable(data);
    }
}
