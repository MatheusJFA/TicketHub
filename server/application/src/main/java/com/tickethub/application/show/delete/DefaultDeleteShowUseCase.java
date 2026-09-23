package com.tickethub.application.show.delete;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultDeleteShowUseCase extends DeleteShowUseCase {
    private final ShowGateway showGateway;

    public DefaultDeleteShowUseCase(final ShowGateway showGateway) {
        this.showGateway = requireNonNull(showGateway);
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
