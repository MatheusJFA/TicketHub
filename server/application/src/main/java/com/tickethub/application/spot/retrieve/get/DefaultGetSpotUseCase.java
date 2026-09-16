package com.tickethub.application.spot.retrieve.get;

import java.util.Optional;

import com.tickethub.domain.core.spot.Spot;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.validation.Error;

public final class DefaultGetSpotUseCase extends GetSpotUseCase {
    private final SpotGateway spotGateway;

    public DefaultGetSpotUseCase(final SpotGateway spotGateway) {
        this.spotGateway = Objects.requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, GetSpotOutput> execute(final String input) {
        try {
            final SpotID id = SpotID.from(input);
            final Optional<Spot> found = spotGateway.findById(id);

            if (!found.isPresent()) {
                return Either.left(notFound("Spot", input));
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
