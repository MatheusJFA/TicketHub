package com.tickethub.application.show.publish;

import java.util.Objects;
import java.util.Optional;

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
        try {
            final Optional<Show> found = showGateway.findById(ShowID.from(command.id()));
            
            if (!found.isPresent()) {
                return Either.left(notFound("Show", command.id()));
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
