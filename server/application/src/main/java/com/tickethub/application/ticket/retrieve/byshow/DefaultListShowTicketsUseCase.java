package com.tickethub.application.ticket.retrieve.byshow;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.validation.Notification;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lists every ticket of a show for door preload ({@code GET
 * /shows/{showId}/tickets}). Tickets whose spot vanished are skipped instead
 * of failing the whole listing. Signature verification still happens
 * server-side on validate; the preload only lets the door match
 * {@code ticketId:code} while offline.
 */
public class DefaultListShowTicketsUseCase extends ListShowTicketsUseCase {
    private final TicketGateway ticketGateway;
    private final SpotGateway spotGateway;

    public DefaultListShowTicketsUseCase(
            final TicketGateway ticketGateway, final SpotGateway spotGateway) {
        this.ticketGateway = requireNonNull(ticketGateway);
        this.spotGateway = requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, List<ListShowTicketsOutput>> execute(
            final ListShowTicketsCommand input) {
        try {
            final var spots = spotGateway.findByShowId(ShowID.from(input.showId()));
            final Map<SpotID, Spot> bySpotId =
                    spots.stream().collect(Collectors.toMap(Spot::getId, Function.identity()));
            final var output = ticketGateway
                    .findBySpotIds(bySpotId.keySet().stream().toList())
                    .stream()
                    .filter(ticket -> bySpotId.containsKey(ticket.getSpotId()))
                    .map(ticket -> ListShowTicketsOutput.from(ticket, bySpotId.get(ticket.getSpotId())))
                    .toList();
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
