package com.natixis.transaction_scheduler.infrastructure.adapter.configuration;

import com.natixis.transaction_scheduler.application.usecase.CreateTransactionUseCaseImpl;
import com.natixis.transaction_scheduler.application.usecase.DeleteTransactionUseCaseImpl;
import com.natixis.transaction_scheduler.application.usecase.GetTransactionUseCaseImpl;
import com.natixis.transaction_scheduler.application.usecase.UpdateTransactionUseCaseImpl;
import com.natixis.transaction_scheduler.domain.port.in.CreateTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.in.DeleteTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.in.GetTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.in.UpdateTransactionUseCase;
import com.natixis.transaction_scheduler.domain.port.out.FeeConfigurationRepository;
import com.natixis.transaction_scheduler.domain.port.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UseCaseConfig {

    private final TransactionRepository transactionRepository;
    private final FeeConfigurationRepository feeConfigurationRepository;

    @Bean
    public CreateTransactionUseCase createCustomerUseCase() {
        return new CreateTransactionUseCaseImpl(transactionRepository, feeConfigurationRepository);
    }

    @Bean
    public UpdateTransactionUseCase updateCustomerUseCase() {
        return new UpdateTransactionUseCaseImpl(transactionRepository, feeConfigurationRepository);
    }

    @Bean
    public GetTransactionUseCase getCustomerUseCase() {
        return new GetTransactionUseCaseImpl(transactionRepository);
    }

    @Bean
    public DeleteTransactionUseCase deleteCustomerUseCase() {
        return new DeleteTransactionUseCaseImpl(transactionRepository);
    }
}