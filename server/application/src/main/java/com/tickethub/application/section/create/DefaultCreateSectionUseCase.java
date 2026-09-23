package com.tickethub.application.section.create;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.validation.Notification;
import java.util.List;

public class DefaultCreateSectionUseCase extends CreateSectionUseCase {
    private final SectionGateway sectionGateway;
    private final ShowGateway showGateway;
    private final int seatNumberWidth;

    public DefaultCreateSectionUseCase(
            final SectionGateway sectionGateway, final ShowGateway showGateway, final int seatNumberWidth) {
        this.sectionGateway = requireNonNull(sectionGateway);
        this.showGateway = requireNonNull(showGateway);
        this.seatNumberWidth = seatNumberWidth;
    }

    @Override
    public Either<Notification, CreateSectionOutput> execute(final CreateSectionCommand command) {
        try {
            final ShowID showId = ShowID.from(command.showId());

            if (showGateway.existsByIds(List.of(showId)).isEmpty()) {
                return Either.left(notFound(Show.class.getSimpleName(), showId.getValue()));
            }

            final Section entity = Section.create(
                    command.name(),
                    command.description(),
                    command.totalSpots(),
                    command.price(),
                    Location.sectionCode(0),
                    seatNumberWidth);

            final Notification notification = Notification.create();

            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Section savedSection = sectionGateway.create(entity, showId);
            final CreateSectionOutput output = CreateSectionOutput.from(savedSection);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
