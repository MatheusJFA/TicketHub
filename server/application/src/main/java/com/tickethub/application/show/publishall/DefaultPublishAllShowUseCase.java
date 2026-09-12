package com.tickethub.application.show.publishall;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public final class DefaultPublishAllShowUseCase extends PublishAllShowUseCase {
    private final ShowGateway showGateway;

    public DefaultPublishAllShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, PublishAllShowOutput> execute(final PublishAllShowCommand command) {
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
            entity.publishAll();
            return Either.right(PublishAllShowOutput.from(showGateway.update(entity)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
