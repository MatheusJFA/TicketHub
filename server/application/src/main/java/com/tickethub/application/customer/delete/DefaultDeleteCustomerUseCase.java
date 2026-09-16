package com.tickethub.application.customer.delete;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;


public final class DefaultDeleteCustomerUseCase extends DeleteCustomerUseCase {
    private final CustomerGateway customerGateway;

    public DefaultDeleteCustomerUseCase(final CustomerGateway customerGateway) {
        this.customerGateway = Objects.requireNonNull(customerGateway);
    }

    @Override
    public Either<Notification, DeleteCustomerOutput> execute(final String input) {
        try {
            final CustomerID id = CustomerID.from(input);
            
            customerGateway.deleteById(id);

            final DeleteCustomerOutput output = new DeleteCustomerOutput(input);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
