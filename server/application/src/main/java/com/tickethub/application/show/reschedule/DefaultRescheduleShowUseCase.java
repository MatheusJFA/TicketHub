package com.tickethub.application.show.reschedule;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Error;

public final class DefaultRescheduleShowUseCase extends RescheduleShowUseCase {
    private final ShowGateway showGateway;

    public DefaultRescheduleShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, RescheduleShowOutput> execute(final RescheduleShowCommand input) {
        Objects.requireNonNull(input);
        try {
            final var id = ShowID.from(input.id());
            final var found = showGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Show not found: " + input.id())));
            }
            final var entity = found.get();
            entity.reschedule(input.date());
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            final var saved = showGateway.update(entity);
            final var output = new RescheduleShowOutput(saved.getId().getValue());
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
