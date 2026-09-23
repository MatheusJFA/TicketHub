package com.tickethub.application.section.generatespots;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Notification;

public class DefaultGenerateSectionSpotsUseCase extends GenerateSectionSpotsUseCase {
    private final ShowGateway showGateway;
    private final int seatNumberWidth;

    public DefaultGenerateSectionSpotsUseCase(final ShowGateway showGateway, final int seatNumberWidth) {
        this.showGateway = requireNonNull(showGateway);
        this.seatNumberWidth = seatNumberWidth;
    }

    @Override
    public Either<Notification, GenerateSectionSpotsOutput> execute(final GenerateSectionSpotsCommand command) {
        try {
            final var found = findOrNotFound(
                    showGateway.findById(ShowID.from(command.showId())), Show.class.getSimpleName(), command.showId());
            if (found.isLeft()) {
                return Either.left(found.getLeft());
            }
            final Show show = found.getRight();
            final var section = show.getSections().stream()
                    .filter(candidate -> candidate.getId().getValue().equals(command.sectionId()))
                    .findFirst();
            if (section.isEmpty()) {
                return Either.left(notFound(Section.class.getSimpleName(), command.sectionId()));
            }
            final long before = section.get().getSpots().size();
            final var generated = section.get().generateMissingSpots(command.sectionCode(), seatNumberWidth);
            if (!generated.isEmpty()) {
                showGateway.appendSpots(show.getId(), section.get().getId(), generated);
            }
            return Either.right(GenerateSectionSpotsOutput.from(command.sectionId(), generated.size()));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
