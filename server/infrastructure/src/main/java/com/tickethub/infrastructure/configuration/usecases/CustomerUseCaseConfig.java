package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.customer.changename.ChangeCustomerNameUseCase;
import com.tickethub.application.customer.changename.DefaultChangeCustomerNameUseCase;
import com.tickethub.application.customer.create.CreateCustomerUseCase;
import com.tickethub.application.customer.create.DefaultCreateCustomerUseCase;
import com.tickethub.application.customer.delete.DefaultDeleteCustomerUseCase;
import com.tickethub.application.customer.delete.DeleteCustomerUseCase;
import com.tickethub.application.customer.retrieve.get.DefaultGetCustomerUseCase;
import com.tickethub.application.customer.retrieve.get.GetCustomerUseCase;
import com.tickethub.application.customer.retrieve.list.DefaultListCustomersUseCase;
import com.tickethub.application.customer.retrieve.list.ListCustomersUseCase;
import com.tickethub.application.customer.update.DefaultUpdateCustomerUseCase;
import com.tickethub.application.customer.update.UpdateCustomerUseCase;
import com.tickethub.domain.authentication.PasswordHasher;
import com.tickethub.domain.core.customer.CustomerGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({CustomerGateway.class})
public class CustomerUseCaseConfig {
    private final CustomerGateway customerGateway;
    private final PasswordHasher passwordHasher;

    public CustomerUseCaseConfig(final CustomerGateway customerGateway, final PasswordHasher passwordHasher) {
        this.customerGateway = customerGateway;
        this.passwordHasher = passwordHasher;
    }

    @Bean
    public ChangeCustomerNameUseCase changeCustomerNameUseCase() {
        return new DefaultChangeCustomerNameUseCase(customerGateway);
    }

    @Bean
    public CreateCustomerUseCase createCustomerUseCase() {
        return new DefaultCreateCustomerUseCase(customerGateway, passwordHasher);
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

    @Bean
    public UpdateCustomerUseCase updateCustomerUseCase() {
        return new DefaultUpdateCustomerUseCase(customerGateway);
    }
}
