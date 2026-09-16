package com.tickethub.application.show.addsection;

import com.tickethub.domain.core.show.Show;

import java.util.Objects;
import java.util.Optional;

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
        try {
            final Optional<Show> found = showGateway.findById(ShowID.from(command.showId()));

            if (!found.isPresent()) {
                return Either.left(notFound("Show", command.showId()));
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
                command.price()
            );

            final Show updatedShow = showGateway.update(entity);
            final AddSectionToShowOutput output = AddSectionToShowOutput.from(updatedShow);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
