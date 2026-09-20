package com.tickethub.application.show.addsection;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SpotsGenerationRequested;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.event.DomainEventPublisher;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.validation.Notification;

public class DefaultAddSectionToShowUseCase extends AddSectionToShowUseCase {

    private final ShowGateway showGateway;
    private final DomainEventPublisher eventPublisher;
    private final long asyncSpotThreshold;
    private final int seatNumberWidth;

    public DefaultAddSectionToShowUseCase(final ShowGateway showGateway,
            final DomainEventPublisher eventPublisher, final long asyncSpotThreshold,
            final int seatNumberWidth) {
        this.showGateway = Objects.requireNonNull(showGateway);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        if (asyncSpotThreshold < 1) {
            throw new IllegalArgumentException("'asyncSpotThreshold' should be positive");
        }
        this.asyncSpotThreshold = asyncSpotThreshold;
        if (seatNumberWidth < 1) {
            throw new IllegalArgumentException("'seatNumberWidth' should be positive");
        }
        this.seatNumberWidth = seatNumberWidth;
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

            if (command.totalSpots() >= asyncSpotThreshold) {
                return addSectionAsync(entity, command);
            }

            final Section candidate = Section.create(command.name(), command.description(),
                    command.totalSpots(), command.price(), Location.sectionCode(0),
                    seatNumberWidth);
            candidate.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }

            entity.addSection(
                    command.name(),
                    command.description(),
                    command.totalSpots(),
                    command.price(),
                    seatNumberWidth);

            final Notification afterMutation = Notification.create();
            entity.validate(afterMutation);
            if (afterMutation.hasError()) {
                return Either.left(afterMutation);
            }

            final Show updatedShow = showGateway.update(entity);
            final AddSectionToShowOutput output = AddSectionToShowOutput.from(updatedShow);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification failed = Notification.create(exception);
            return Either.left(failed);
        }
    }

    private Either<Notification, AddSectionToShowOutput> addSectionAsync(final Show entity,
            final AddSectionToShowCommand command) {
        final Section candidate = Section.createShell(command.name(), command.description(),
                command.totalSpots(), command.price());
        final Notification validation = Notification.create();
        candidate.validate(validation);
        if (validation.hasError()) {
            return Either.left(validation);
        }
        final String sectionCode = Location.sectionCode(entity.getSections().size());
        final Section shell = entity.addSectionShell(
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
        try {
            eventPublisher.publish(new SpotsGenerationRequested(
                    entity.getId().getValue(),
                    shell.getId().getValue(),
                    sectionCode,
                    command.totalSpots(),
                    Instant.now()));
        } catch (final RuntimeException publishFailure) {
            final var generated = shell.generateMissingSpots(sectionCode, seatNumberWidth);
            showGateway.appendSpots(entity.getId(), shell.getId(), generated);
        }
        return Either.right(AddSectionToShowOutput.from(updatedShow));
    }

}
