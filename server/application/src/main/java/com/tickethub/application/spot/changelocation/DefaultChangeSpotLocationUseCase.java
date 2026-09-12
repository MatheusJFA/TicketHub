package com.tickethub.application.spot.changelocation;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.validation.Error;

public final class DefaultChangeSpotLocationUseCase extends ChangeSpotLocationUseCase {
    private final SpotGateway spotGateway;

    public DefaultChangeSpotLocationUseCase(final SpotGateway spotGateway) {
        this.spotGateway = Objects.requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, ChangeSpotLocationOutput> execute(final ChangeSpotLocationCommand input) {
        Objects.requireNonNull(input);
        try {
            final var id = SpotID.from(input.id());
            final var found = spotGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Spot not found: " + input.id())));
            }
            final var entity = found.get();
            entity.changeLocation(input.location());
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            final var saved = spotGateway.update(entity);
            final var output = new ChangeSpotLocationOutput(saved.getId().getValue());
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
