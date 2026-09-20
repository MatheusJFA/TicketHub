package com.tickethub.application.customer.create;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.authentication.PasswordHasher;
import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.validation.Notification;

public class DefaultCreateCustomerUseCase extends CreateCustomerUseCase {
    private final CustomerGateway customerGateway;
    private final PasswordHasher passwordHasher;

    public DefaultCreateCustomerUseCase(final CustomerGateway customerGateway, final PasswordHasher passwordHasher) {
        this.customerGateway = requireNonNull(customerGateway);
        this.passwordHasher = requireNonNull(passwordHasher);
    }

    @Override
    public Either<Notification, CreateCustomerOutput> execute(final CreateCustomerCommand command) {
        try {
            final Customer entity = Customer.create(command.cpf(), command.name(), command.email(),
                    passwordHasher.hash(command.password()));
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
