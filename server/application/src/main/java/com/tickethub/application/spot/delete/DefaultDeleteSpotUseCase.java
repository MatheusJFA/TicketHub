package com.tickethub.application.spot.delete;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultDeleteSpotUseCase extends DeleteSpotUseCase {
    private final SpotGateway spotGateway;

    public DefaultDeleteSpotUseCase(final SpotGateway spotGateway) {
        this.spotGateway = requireNonNull(spotGateway);
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
