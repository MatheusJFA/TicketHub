package com.tickethub.application.spot.publish;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.validation.Notification;

public final class DefaultPublishSpotUseCase extends PublishSpotUseCase {
    private final SpotGateway spotGateway;

    public DefaultPublishSpotUseCase(final SpotGateway spotGateway) {
        this.spotGateway = Objects.requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, PublishSpotOutput> execute(final PublishSpotCommand command) {
        try {
            final SpotID id = SpotID.from(command.id());
            final Optional<Spot> found = spotGateway.findById(id);

            if (!found.isPresent()) {
                return Either.left(notFound(Spot.class.getSimpleName(), id.getValue()));
            }

            final Spot entity = found.get();
            final Notification notification = Notification.create();

            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }

            entity.publish();
            final Spot updatedSpot = spotGateway.update(entity);
            final PublishSpotOutput output = PublishSpotOutput.from(updatedSpot);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
