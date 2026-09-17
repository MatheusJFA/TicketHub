package com.tickethub.application.show.unpublishall;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Notification;

public final class DefaultUnpublishAllShowUseCase extends UnpublishAllShowUseCase {
    private final ShowGateway showGateway;

    public DefaultUnpublishAllShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, UnpublishAllShowOutput> execute(final UnpublishAllShowCommand command) {
        try {
            final ShowID id = ShowID.from(command.id());
            final Optional<Show> found = showGateway.findById(id);

            if (!found.isPresent()) {
                return Either.left(notFound(Show.class.getSimpleName(), id.getValue()));
            }

            final Show entity = found.get();

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            entity.unpublishAll();

            final Show updatedShow = showGateway.update(entity);
            final UnpublishAllShowOutput output = UnpublishAllShowOutput.from(updatedShow);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
