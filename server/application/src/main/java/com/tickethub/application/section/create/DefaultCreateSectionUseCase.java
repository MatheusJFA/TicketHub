package com.tickethub.application.section.create;

import java.util.List;
import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public class DefaultCreateSectionUseCase extends CreateSectionUseCase {
    private final SectionGateway sectionGateway;
    private final ShowGateway showGateway;

    public DefaultCreateSectionUseCase(final SectionGateway sectionGateway, final ShowGateway showGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
        this.showGateway = Objects.requireNonNull(showGateway);
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
                command.price()
            );

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
