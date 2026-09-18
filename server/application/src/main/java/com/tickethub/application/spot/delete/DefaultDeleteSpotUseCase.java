package com.tickethub.application.spot.delete;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;


public final class DefaultDeleteSpotUseCase extends DeleteSpotUseCase {
    private final SpotGateway spotGateway;

    public DefaultDeleteSpotUseCase(final SpotGateway spotGateway) {
        this.spotGateway = Objects.requireNonNull(spotGateway);
    }

    @Override
    public Optional<Notification> execute(final String input) {
        try {
            final SpotID id = SpotID.from(input);
            spotGateway.deleteById(id);
            return Optional.empty();
        } catch (final RuntimeException exception) {
            return Optional.of(Notification.create(exception));
        }
    }
}
