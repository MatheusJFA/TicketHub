package com.tickethub.application.spot.create;

import static java.util.Objects.requireNonNull;
import java.util.List;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.validation.Error;

public class DefaultCreateSpotUseCase extends CreateSpotUseCase {
    private final SpotGateway spotGateway;
    private final SectionGateway sectionGateway;
    private final int seatNumberWidth;

    public DefaultCreateSpotUseCase(final SpotGateway spotGateway, final SectionGateway sectionGateway,
            final int seatNumberWidth) {
        this.spotGateway = requireNonNull(spotGateway);
        this.sectionGateway = requireNonNull(sectionGateway);
        if (seatNumberWidth < 1) {
            throw new IllegalArgumentException("'seatNumberWidth' should be positive");
        }
        this.seatNumberWidth = seatNumberWidth;
    }

    @Override
    public Either<Notification, CreateSpotOutput> execute(final CreateSpotCommand command) {
        try {
            final SectionID sectionId = SectionID.from(command.sectionId());

            if (sectionGateway.existsByIds(List.of(sectionId)).isEmpty()) {
                return Either.left(notFound(Section.class.getSimpleName(), sectionId.getValue()));
            }

            // A missing location generates a short hash-based code (see Location).
            final Spot entity = Optional.ofNullable(command.location()).map(Spot::create)
                    .orElseGet(() -> Spot.create(seatNumberWidth));
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
