package com.tickethub.application.customer.changename;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.validation.Error;

public final class DefaultChangeCustomerNameUseCase extends ChangeCustomerNameUseCase {
    private final CustomerGateway customerGateway;

    public DefaultChangeCustomerNameUseCase(final CustomerGateway customerGateway) {
        this.customerGateway = Objects.requireNonNull(customerGateway);
    }

    @Override
    public Either<Notification, ChangeCustomerNameOutput> execute(final ChangeCustomerNameCommand input) {
        Objects.requireNonNull(input);
        try {
            final var id = CustomerID.from(input.id());
            final var found = customerGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Customer not found: " + input.id())));
            }
            final var entity = found.get();
            entity.changeName(input.name());
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            final var saved = customerGateway.update(entity);
            final var output = new ChangeCustomerNameOutput(saved.getId().getValue());
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
