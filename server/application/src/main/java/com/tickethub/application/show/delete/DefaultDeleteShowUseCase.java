package com.tickethub.application.show.delete;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;


public final class DefaultDeleteShowUseCase extends DeleteShowUseCase {
    private final ShowGateway showGateway;

    public DefaultDeleteShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Optional<Notification> execute(final String input) {
        try {
            final ShowID id = ShowID.from(input);
            showGateway.deleteById(id);
            return Optional.empty();
        } catch (final RuntimeException exception) {
            return Optional.of(Notification.create(exception));
        }
    }
}
