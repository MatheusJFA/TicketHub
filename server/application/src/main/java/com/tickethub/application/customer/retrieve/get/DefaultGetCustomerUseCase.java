package com.tickethub.application.customer.retrieve.get;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.validation.Notification;

public class DefaultGetCustomerUseCase extends GetCustomerUseCase {
    private final CustomerGateway customerGateway;

    public DefaultGetCustomerUseCase(final CustomerGateway customerGateway) {
        this.customerGateway = Objects.requireNonNull(customerGateway);
    }

    @Override
    public Either<Notification, GetCustomerOutput> execute(final String input) {
        try {
            final CustomerID id = CustomerID.from(input);

            final Optional<Customer> found = customerGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(notFound(Customer.class.getSimpleName(), id.getValue()));
            }
            
            final Customer entity = found.get();
            final GetCustomerOutput output = GetCustomerOutput.from(entity);
            return Either.right(output);
        } catch (final RuntimeException exception) {           
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
