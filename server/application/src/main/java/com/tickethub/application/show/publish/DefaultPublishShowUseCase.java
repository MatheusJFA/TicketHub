package com.tickethub.application.show.publish;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultPublishShowUseCase extends PublishShowUseCase {
    private final ShowGateway showGateway;

    public DefaultPublishShowUseCase(final ShowGateway showGateway) {
        this.showGateway = requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, PublishShowOutput> execute(final PublishShowCommand command) {
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

            entity.publish();
            final Show updatedShow = showGateway.update(entity);
            final PublishShowOutput output = PublishShowOutput.from(updatedShow);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
