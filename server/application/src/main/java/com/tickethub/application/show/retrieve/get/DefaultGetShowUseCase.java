package com.tickethub.application.show.retrieve.get;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Error;

public final class DefaultGetShowUseCase extends GetShowUseCase {
    private final ShowGateway showGateway;

    public DefaultGetShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, GetShowOutput> execute(final String input) {
        Objects.requireNonNull(input);
        try {
            final var id = ShowID.from(input);
            final var found = showGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Show not found: " + input)));
            }
            final var entity = found.get();
            final var output = GetShowOutput.from(entity);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
