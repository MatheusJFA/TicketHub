package com.tickethub.application.spot.unpublish;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public final class DefaultUnpublishSpotUseCase extends UnpublishSpotUseCase {
    private final SpotGateway spotGateway;

    public DefaultUnpublishSpotUseCase(final SpotGateway spotGateway) {
        this.spotGateway = Objects.requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, UnpublishSpotOutput> execute(final UnpublishSpotCommand command) {
        Objects.requireNonNull(command);
        try {
            final var found = spotGateway.findById(SpotID.from(command.id()));
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Spot not found: " + command.id())));
            }
            final var entity = found.get();
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            entity.unpublish();
            return Either.right(UnpublishSpotOutput.from(spotGateway.update(entity)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
