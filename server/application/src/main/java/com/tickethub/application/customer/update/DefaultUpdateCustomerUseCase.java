package com.tickethub.application.customer.update;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultUpdateCustomerUseCase extends UpdateCustomerUseCase {
    private final CustomerGateway customerGateway;

    public DefaultUpdateCustomerUseCase(final CustomerGateway customerGateway) {
        this.customerGateway = requireNonNull(customerGateway);
    }

    @Override
    public Either<Notification, UpdateCustomerOutput> execute(final UpdateCustomerCommand input) {
        try {
            final CustomerID id = CustomerID.from(input.id());
            final Optional<Customer> found = customerGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Customer.class.getSimpleName(), id.getValue()));
            }

            final Customer entity = found.get();
            entity.changeName(input.name());

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Customer saved = customerGateway.update(entity);
            final UpdateCustomerOutput output = UpdateCustomerOutput.from(saved);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
