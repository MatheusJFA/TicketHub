package com.tickethub.application.customer.changename;

import static java.util.Objects.requireNonNull;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.core.customer.CustomerID;

public class DefaultChangeCustomerNameUseCase extends ChangeCustomerNameUseCase {
    private final CustomerGateway customerGateway;

    public DefaultChangeCustomerNameUseCase(final CustomerGateway customerGateway) {
        this.customerGateway = requireNonNull(customerGateway);
    }

    @Override
    public Either<Notification, ChangeCustomerNameOutput> execute(final ChangeCustomerNameCommand input) {
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

            final Customer savedCustomer = customerGateway.update(entity);
            final ChangeCustomerNameOutput output = ChangeCustomerNameOutput.from(savedCustomer);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
