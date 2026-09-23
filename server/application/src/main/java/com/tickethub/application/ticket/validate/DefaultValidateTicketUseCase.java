package com.tickethub.application.ticket.validate;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotPlacement;
import com.tickethub.domain.core.ticket.Ticket;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.core.ticket.TicketID;
import com.tickethub.domain.core.ticket.TicketSigner;
import com.tickethub.domain.core.ticket.TicketStatus;
import com.tickethub.domain.exception.InvalidTicketSignatureException;
import com.tickethub.domain.exception.ResourceNotFoundException;
import com.tickethub.domain.exception.ShowOutsideCheckInDateException;
import com.tickethub.domain.exception.SpotOwnershipException;
import com.tickethub.domain.exception.TicketAlreadyUsedException;
import com.tickethub.domain.validation.Notification;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

/**
 * Door validation for ticket QR codes. Proves the QR was issued by this
 * system (signature), that the ticket is still unused, that its spot really
 * belongs to the show being controlled, and that the show is happening
 * today. Then checks both ticket and spot in so the QR cannot be reused.
 */
public class DefaultValidateTicketUseCase extends ValidateTicketUseCase {
    private final TicketGateway ticketGateway;
    private final TicketSigner ticketSigner;
    private final SpotGateway spotGateway;
    private final ShowGateway showGateway;
    private final Clock clock;

    public DefaultValidateTicketUseCase(
            final TicketGateway ticketGateway,
            final TicketSigner ticketSigner,
            final SpotGateway spotGateway,
            final ShowGateway showGateway) {
        this(ticketGateway, ticketSigner, spotGateway, showGateway, Clock.systemUTC());
    }

    public DefaultValidateTicketUseCase(
            final TicketGateway ticketGateway,
            final TicketSigner ticketSigner,
            final SpotGateway spotGateway,
            final ShowGateway showGateway,
            final Clock clock) {
        this.ticketGateway = requireNonNull(ticketGateway, "'ticketGateway' should not be null");
        this.ticketSigner = requireNonNull(ticketSigner, "'ticketSigner' should not be null");
        this.spotGateway = requireNonNull(spotGateway, "'spotGateway' should not be null");
        this.showGateway = requireNonNull(showGateway, "'showGateway' should not be null");
        this.clock = requireNonNull(clock, "'clock' should not be null");
    }

    @Override
    public Either<Notification, ValidateTicketOutput> execute(final ValidateTicketCommand command) {
        try {
            final TicketID ticketId = TicketID.from(command.ticketId());
            final var stored = ticketGateway.findById(ticketId);
            if (stored.isEmpty()) {
                throw new ResourceNotFoundException(Ticket.class.getSimpleName(), ticketId.getValue());
            }
            final Ticket ticket = stored.get();
            if (!Objects.equals(command.code(), ticket.getCode())) {
                throw new InvalidTicketSignatureException();
            }
            ticket.verifySignature(ticketSigner);
            if (ticket.getStatus() == TicketStatus.USED) {
                throw new TicketAlreadyUsedException();
            }

            final SpotPlacement placement = spotGateway
                    .findPlacement(ticket.getSpotId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            Spot.class.getSimpleName(), ticket.getSpotId().getValue()));
            if (!Objects.equals(command.showId(), placement.showId())) {
                throw new SpotOwnershipException();
            }

            final ShowID showId = ShowID.from(command.showId());
            final Show entity = showGateway
                    .findById(showId)
                    .orElseThrow(() -> new ResourceNotFoundException(Show.class.getSimpleName(), showId.getValue()));

            final var showDate = entity.getDate();
            final var venueZone =
                    Optional.ofNullable(showDate).map(OffsetDateTime::getOffset).orElse(ZoneOffset.UTC);
            final var todayAtVenue = LocalDate.ofInstant(clock.instant(), venueZone);
            if (isNull(showDate) || !showDate.toLocalDate().isEqual(todayAtVenue)) {
                throw new ShowOutsideCheckInDateException(showDate);
            }

            ticket.checkIn();
            ticketGateway.update(ticket);

            final Spot spot = placement.spot();
            spot.checkIn();
            final Spot updated = spotGateway.update(spot);
            return Either.right(ValidateTicketOutput.from(command.showId(), ticket, updated, entity.getDate()));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
