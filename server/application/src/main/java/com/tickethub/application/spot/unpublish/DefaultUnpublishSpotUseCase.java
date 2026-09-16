package com.tickethub.application.spot.unpublish;

import java.util.Objects;
import java.util.Optional;

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
        try {
            final String id = command.id();
            final Optional<Spot> found = spotGateway.findById(SpotID.from(id));
            if (!found.isPresent()) {
                return Either.left(notFound("Spot", id));
            }
            final Spot entity = found.get();
            final Notification notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            entity.unpublish();
            return Either.right(UnpublishSpotOutput.from(spotGateway.update(entity)));
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }



}
