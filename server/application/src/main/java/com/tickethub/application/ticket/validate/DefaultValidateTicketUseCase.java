package com.tickethub.application.ticket.validate;

import static java.util.Objects.requireNonNull;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.spot.SpotPlacement;
import com.tickethub.domain.exception.ResourceNotFoundException;
import com.tickethub.domain.exception.ShowOutsideCheckInDateException;
import com.tickethub.domain.exception.SpotOwnershipException;
import com.tickethub.domain.validation.Notification;
import java.util.Objects;

/**
 * Door validation for guest QR codes. Confirms the ticket really exists for
 * the presented show and section, that the show is happening today, and then
 * checks the spot in (marks it used) so the same QR code cannot be reused.
 */
public class DefaultValidateTicketUseCase extends ValidateTicketUseCase {
    private final SpotGateway spotGateway;
    private final ShowGateway showGateway;
    private final Clock clock;

    public DefaultValidateTicketUseCase(final SpotGateway spotGateway, final ShowGateway showGateway) {
        this(spotGateway, showGateway, Clock.systemUTC());
    }

    public DefaultValidateTicketUseCase(final SpotGateway spotGateway, final ShowGateway showGateway,
            final Clock clock) {
        this.spotGateway = requireNonNull(spotGateway);
        this.showGateway = requireNonNull(showGateway, "'showGateway' should not be null");
        this.clock = requireNonNull(clock, "'clock' should not be null");
    }

    @Override
    public Either<Notification, ValidateTicketOutput> execute(final ValidateTicketCommand command) {
        try {
            final SpotID spotId = SpotID.from(command.spotId());
            final Optional<SpotPlacement> placement = spotGateway.findPlacement(spotId);

            if (placement.isEmpty()) {
                throw new ResourceNotFoundException(Spot.class.getSimpleName(), spotId.getValue());
            }

            final SpotPlacement found = placement.get();
            if (!Objects.equals(command.showId(), found.showId())
                    || !Objects.equals(command.sectionId(), found.sectionId())) {
                throw new SpotOwnershipException();
            }

            final ShowID showId = ShowID.from(command.showId());
            final Optional<Show> show = showGateway.findById(showId);

            if (show.isEmpty()) {
                throw new ResourceNotFoundException(Show.class.getSimpleName(), showId.getValue());
            }

            final Show entity = show.get();
            final var showDate = entity.getDate();
            final var todayAtVenue = LocalDate.ofInstant(clock.instant(),
                    showDate != null ? showDate.getOffset() : ZoneOffset.UTC);
            if (showDate == null || !showDate.toLocalDate().isEqual(todayAtVenue)) {
                throw new ShowOutsideCheckInDateException(showDate);
            }

            final Spot spot = found.spot();
            spot.checkIn();

            final Spot updated = spotGateway.update(spot);
            return Either.right(ValidateTicketOutput.from(command.showId(), command.sectionId(),
                    updated, entity.getDate()));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
