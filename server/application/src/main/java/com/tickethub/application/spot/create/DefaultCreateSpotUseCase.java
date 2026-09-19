package com.tickethub.application.spot.create;

import java.util.List;
import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public class DefaultCreateSpotUseCase extends CreateSpotUseCase {
    private final SpotGateway spotGateway;
    private final SectionGateway sectionGateway;

    public DefaultCreateSpotUseCase(final SpotGateway spotGateway, final SectionGateway sectionGateway) {
        this.spotGateway = Objects.requireNonNull(spotGateway);
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, CreateSpotOutput> execute(final CreateSpotCommand command) {
        try {
            final SectionID sectionId = SectionID.from(command.sectionId());

            if (sectionGateway.existsByIds(List.of(sectionId)).isEmpty()) {
                return Either.left(notFound(Section.class.getSimpleName(), sectionId.getValue()));
            }

            // A missing location generates a short hash-based code (see Location).
            final Spot entity = command.location() == null ? Spot.create() : Spot.create(command.location());
            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            return Either.right(CreateSpotOutput.from(spotGateway.create(entity, sectionId)));
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
