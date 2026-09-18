package com.tickethub.application.spot.create;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public final class DefaultCreateSpotUseCase extends CreateSpotUseCase {
    private final SpotGateway spotGateway;

    public DefaultCreateSpotUseCase(final SpotGateway spotGateway) {
        this.spotGateway = Objects.requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, CreateSpotOutput> execute(final CreateSpotCommand command) {
        try {
            // A missing location generates a short hash-based code (see Location).
            final Spot entity = command.location() == null ? Spot.create() : Spot.create(command.location());
            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            return Either.right(CreateSpotOutput.from(spotGateway.create(entity)));
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
