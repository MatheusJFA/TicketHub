package com.tickethub.application.spot.retrieve.get;

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
        Objects.requireNonNull(input);
        try {
            final var id = SpotID.from(input);
            final var found = spotGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Spot not found: " + input)));
            }
            final var entity = found.get();
            final var output = GetSpotOutput.from(entity);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
