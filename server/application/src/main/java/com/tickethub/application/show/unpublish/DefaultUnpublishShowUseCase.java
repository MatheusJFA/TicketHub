package com.tickethub.application.show.unpublish;

import static java.util.Objects.requireNonNull;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Notification;

public class DefaultUnpublishShowUseCase extends UnpublishShowUseCase {
    private final ShowGateway showGateway;

    public DefaultUnpublishShowUseCase(final ShowGateway showGateway) {
        this.showGateway = requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, UnpublishShowOutput> execute(final UnpublishShowCommand command) {
        try {
            final ShowID id = ShowID.from(command.id());

            final Optional<Show> found = showGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(notFound(Show.class.getSimpleName(), id.getValue()));
            }

            final Show entity = found.get();
            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            entity.unpublish();

            final Show updatedShow = showGateway.update(entity);
            final UnpublishShowOutput output = UnpublishShowOutput.from(updatedShow);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
