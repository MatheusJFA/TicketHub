package com.tickethub.application.spot.retrieve.get;

import static java.util.Objects.requireNonNull;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.validation.Notification;

public class DefaultGetSpotUseCase extends GetSpotUseCase {
    private final SpotGateway spotGateway;

    public DefaultGetSpotUseCase(final SpotGateway spotGateway) {
        this.spotGateway = requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, GetSpotOutput> execute(final String input) {
        try {
            final SpotID id = SpotID.from(input);
            final Optional<Spot> found = spotGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Spot.class.getSimpleName(), id.getValue()));
            }

            final Spot entity = found.get();
            final GetSpotOutput output = GetSpotOutput.from(entity);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
