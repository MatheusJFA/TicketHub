package com.tickethub.application.show.retrieve.get;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultGetShowUseCase extends GetShowUseCase {
    private final ShowGateway showGateway;

    public DefaultGetShowUseCase(final ShowGateway showGateway) {
        this.showGateway = requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, GetShowOutput> execute(final String input) {
        try {
            final ShowID id = ShowID.from(input);
            final Optional<Show> found = showGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Show.class.getSimpleName(), id.getValue()));
            }

            final Show entity = found.get();
            final GetShowOutput output = GetShowOutput.from(entity);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
