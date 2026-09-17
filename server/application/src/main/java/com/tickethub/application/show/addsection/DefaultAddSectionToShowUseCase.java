package com.tickethub.application.show.addsection;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Notification;

public final class DefaultAddSectionToShowUseCase extends AddSectionToShowUseCase {
    private final ShowGateway showGateway;

    public DefaultAddSectionToShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, AddSectionToShowOutput> execute(final AddSectionToShowCommand command) {
        try {
            final ShowID id = ShowID.from(command.showId());
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

            final Section section = Section.create(command.name(), command.description(), false,
                    command.totalSpots(), 0, command.price(), null);
            section.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            entity.addSection(
                    command.name(),
                    command.description(),
                    command.totalSpots(),
                    command.price());

            final Notification afterMutation = Notification.create();
            entity.validate(afterMutation);
            if (afterMutation.hasError()) {
                return Either.left(afterMutation);
            }

            final Show updatedShow = showGateway.update(entity);
            final AddSectionToShowOutput output = AddSectionToShowOutput.from(updatedShow);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
