package com.tickethub.application.show.delete;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;


public final class DefaultDeleteShowUseCase extends DeleteShowUseCase {
    private final ShowGateway showGateway;

    public DefaultDeleteShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, DeleteShowOutput> execute(final String input) {
        Objects.requireNonNull(input);
        try {
            final var id = ShowID.from(input);
            showGateway.deleteById(id);
            return Either.right(new DeleteShowOutput(input));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
