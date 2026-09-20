package com.tickethub.application.customer.delete;

import static java.util.Objects.requireNonNull;
import java.util.Optional;

import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;


public class DefaultDeleteCustomerUseCase extends DeleteCustomerUseCase {
    private final CustomerGateway customerGateway;

    public DefaultDeleteCustomerUseCase(final CustomerGateway customerGateway) {
        this.customerGateway = requireNonNull(customerGateway);
    }

    @Override
    public Optional<Notification> execute(final String input) {
        try {
            final CustomerID id = CustomerID.from(input);
            customerGateway.deleteById(id);
            return Optional.empty();
        } catch (final RuntimeException exception) {
            return Optional.of(Notification.create(exception));
        }
    }
}
