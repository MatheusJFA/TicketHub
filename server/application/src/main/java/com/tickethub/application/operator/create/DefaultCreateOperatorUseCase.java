package com.tickethub.application.operator.create;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.authentication.PasswordHasher;
import com.tickethub.domain.core.operator.Operator;
import com.tickethub.domain.core.operator.OperatorGateway;
import com.tickethub.domain.validation.Notification;

public class DefaultCreateOperatorUseCase extends CreateOperatorUseCase {
    private final OperatorGateway operatorGateway;
    private final PasswordHasher passwordHasher;

    public DefaultCreateOperatorUseCase(final OperatorGateway operatorGateway, final PasswordHasher passwordHasher) {
        this.operatorGateway = requireNonNull(operatorGateway);
        this.passwordHasher = requireNonNull(passwordHasher);
    }

    @Override
    public Either<Notification, CreateOperatorOutput> execute(final CreateOperatorCommand command) {
        try {
            final Operator entity =
                    Operator.create(command.name(), command.email(), passwordHasher.hash(command.password()));

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Operator createdOperator = operatorGateway.create(entity);
            final CreateOperatorOutput output = CreateOperatorOutput.from(createdOperator);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
