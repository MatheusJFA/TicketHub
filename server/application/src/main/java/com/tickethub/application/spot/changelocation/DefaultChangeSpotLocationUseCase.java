package com.tickethub.application.spot.changelocation;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultChangeSpotLocationUseCase extends ChangeSpotLocationUseCase {
    private final SpotGateway spotGateway;

    public DefaultChangeSpotLocationUseCase(final SpotGateway spotGateway) {
        this.spotGateway = requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, ChangeSpotLocationOutput> execute(final ChangeSpotLocationCommand input) {
        try {
            final SpotID id = SpotID.from(input.id());
            final Optional<Spot> found = spotGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Spot.class.getSimpleName(), id.getValue()));
            }

            final Spot entity = found.get();
            entity.changeLocation(input.location());

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Spot saved = spotGateway.update(entity);
            final ChangeSpotLocationOutput output =
                    new ChangeSpotLocationOutput(saved.getId().getValue());
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
