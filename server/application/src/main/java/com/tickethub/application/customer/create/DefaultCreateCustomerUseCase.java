package com.tickethub.application.customer.create;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.validation.Notification;

public final class DefaultCreateCustomerUseCase extends CreateCustomerUseCase {
    private final CustomerGateway customerGateway;

    public DefaultCreateCustomerUseCase(final CustomerGateway customerGateway) {
        this.customerGateway = Objects.requireNonNull(customerGateway);
    }

    @Override
    public Either<Notification, CreateCustomerOutput> execute(final CreateCustomerCommand command) {
        try {
            final Customer entity = Customer.create(command.cpf(), command.name());
            final Notification notification = Notification.create();

            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            return Either.right(CreateCustomerOutput.from(customerGateway.create(entity)));
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
