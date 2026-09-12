package com.tickethub.application.show.addsection;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public final class DefaultAddSectionToShowUseCase extends AddSectionToShowUseCase {
    private final ShowGateway showGateway;

    public DefaultAddSectionToShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, AddSectionToShowOutput> execute(final AddSectionToShowCommand command) {
        Objects.requireNonNull(command);
        try {
            final var found = showGateway.findById(ShowID.from(command.showId()));
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Show not found: " + command.showId())));
            }
            final var entity = found.get();
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            final var section = Section.create(command.name(), command.description(), false,
                    command.totalSpots(), 0, command.price(), null);
            section.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            entity.addSection(command.name(), command.description(), command.totalSpots(), command.price());
            return Either.right(AddSectionToShowOutput.from(showGateway.update(entity)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
