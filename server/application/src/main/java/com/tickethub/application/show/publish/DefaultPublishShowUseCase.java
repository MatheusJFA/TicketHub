package com.tickethub.application.show.publish;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public final class DefaultPublishShowUseCase extends PublishShowUseCase {
    private final ShowGateway showGateway;

    public DefaultPublishShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, PublishShowOutput> execute(final PublishShowCommand command) {
        Objects.requireNonNull(command);
        try {
            final var found = showGateway.findById(ShowID.from(command.id()));
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Show not found: " + command.id())));
            }
            final var entity = found.get();
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            entity.publish();
            return Either.right(PublishShowOutput.from(showGateway.update(entity)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
