package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.customer.changename.ChangeCustomerNameUseCase;
import com.tickethub.application.customer.changename.DefaultChangeCustomerNameUseCase;
import com.tickethub.application.customer.create.CreateCustomerUseCase;
import com.tickethub.application.customer.create.DefaultCreateCustomerUseCase;
import com.tickethub.application.customer.delete.DeleteCustomerUseCase;
import com.tickethub.application.customer.delete.DefaultDeleteCustomerUseCase;
import com.tickethub.application.customer.retrieve.get.GetCustomerUseCase;
import com.tickethub.application.customer.retrieve.get.DefaultGetCustomerUseCase;
import com.tickethub.application.customer.retrieve.list.ListCustomersUseCase;
import com.tickethub.application.customer.retrieve.list.DefaultListCustomersUseCase;
import com.tickethub.domain.core.customer.CustomerGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({CustomerGateway.class})
public class CustomerUseCaseConfig {
    private final CustomerGateway customerGateway;

    public CustomerUseCaseConfig(final CustomerGateway customerGateway) {
        this.customerGateway = customerGateway;
    }

    @Bean
    public ChangeCustomerNameUseCase changeCustomerNameUseCase() {
        return new DefaultChangeCustomerNameUseCase(customerGateway);
    }

    @Bean
    public CreateCustomerUseCase createCustomerUseCase() {
        return new DefaultCreateCustomerUseCase(customerGateway);
    }

    @Bean
    public DeleteCustomerUseCase deleteCustomerUseCase() {
        return new DefaultDeleteCustomerUseCase(customerGateway);
    }

    @Bean
    public GetCustomerUseCase getCustomerUseCase() {
        return new DefaultGetCustomerUseCase(customerGateway);
    }

    @Bean
    public ListCustomersUseCase listCustomersUseCase() {
        return new DefaultListCustomersUseCase(customerGateway);
    }
}
