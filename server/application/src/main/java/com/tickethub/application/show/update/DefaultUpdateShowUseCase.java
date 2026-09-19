package com.tickethub.application.show.update;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Notification;

public class DefaultUpdateShowUseCase extends UpdateShowUseCase {
    private final ShowGateway showGateway;

    public DefaultUpdateShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, UpdateShowOutput> execute(final UpdateShowCommand input) {
        try {
            final ShowID id = ShowID.from(input.id());
            final Optional<Show> found = showGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Show.class.getSimpleName(), id.getValue()));
            }

            final Show entity = found.get();
            entity.changeName(input.name());
            entity.changeDescription(input.description());
            entity.reschedule(input.date());

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Show saved = showGateway.update(entity);
            final UpdateShowOutput output = UpdateShowOutput.from(saved);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
