package com.tickethub.application.spot.delete;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;


public final class DefaultDeleteSpotUseCase extends DeleteSpotUseCase {
    private final SpotGateway spotGateway;

    public DefaultDeleteSpotUseCase(final SpotGateway spotGateway) {
        this.spotGateway = Objects.requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, DeleteSpotOutput> execute(final String input) {
        try {
            final SpotID id = SpotID.from(input);
            spotGateway.deleteById(id);
            final DeleteSpotOutput output = DeleteSpotOutput.from(input);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
