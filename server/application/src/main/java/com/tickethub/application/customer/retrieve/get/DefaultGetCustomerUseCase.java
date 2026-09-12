package com.tickethub.application.customer.retrieve.get;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.validation.Error;

public final class DefaultGetCustomerUseCase extends GetCustomerUseCase {
    private final CustomerGateway customerGateway;

    public DefaultGetCustomerUseCase(final CustomerGateway customerGateway) {
        this.customerGateway = Objects.requireNonNull(customerGateway);
    }

    @Override
    public Either<Notification, GetCustomerOutput> execute(final String input) {
        Objects.requireNonNull(input);
        try {
            final var id = CustomerID.from(input);
            final var found = customerGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Customer not found: " + input)));
            }
            final var entity = found.get();
            final var output = GetCustomerOutput.from(entity);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
