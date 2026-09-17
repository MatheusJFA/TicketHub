package com.tickethub.application.show.reschedule;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Notification;

public final class DefaultRescheduleShowUseCase extends RescheduleShowUseCase {
    private final ShowGateway showGateway;

    public DefaultRescheduleShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, RescheduleShowOutput> execute(final RescheduleShowCommand input) {
        try {
            final ShowID id = ShowID.from(input.id());
            final Optional<Show> found = showGateway.findById(id);

            if (!found.isPresent()) {
                return Either.left(notFound(Show.class.getSimpleName(), id.getValue()));
            }

            final Show entity = found.get();
            final java.time.OffsetDateTime date = input.date();

            entity.reschedule(date);

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Show saved = showGateway.update(entity);
            final RescheduleShowOutput output = new RescheduleShowOutput(saved.getId().getValue());
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
