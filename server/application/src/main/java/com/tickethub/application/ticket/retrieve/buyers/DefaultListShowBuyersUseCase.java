package com.tickethub.application.ticket.retrieve.buyers;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.customer.CustomerGateway;
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
 * Lists every buyer of a show (name, CPF, e-mail per ticket) for the door
 * guest list. Tickets whose spot vanished and tickets whose customer vanished
 * are skipped instead of failing the whole listing.
 */
public class DefaultListShowBuyersUseCase extends ListShowBuyersUseCase {
    private final TicketGateway ticketGateway;
    private final SpotGateway spotGateway;
    private final CustomerGateway customerGateway;

    public DefaultListShowBuyersUseCase(
            final TicketGateway ticketGateway,
            final SpotGateway spotGateway,
            final CustomerGateway customerGateway) {
        this.ticketGateway = requireNonNull(ticketGateway, "'ticketGateway' should not be null");
        this.spotGateway = requireNonNull(spotGateway, "'spotGateway' should not be null");
        this.customerGateway =
                requireNonNull(customerGateway, "'customerGateway' should not be null");
    }

    @Override
    public Either<Notification, List<ListShowBuyersOutput>> execute(
            final ListShowBuyersCommand input) {
        try {
            final var spots = spotGateway.findByShowId(ShowID.from(input.showId()));
            final Map<SpotID, Spot> bySpotId =
                    spots.stream().collect(Collectors.toMap(Spot::getId, Function.identity()));
            final var tickets = ticketGateway.findBySpotIds(bySpotId.keySet().stream().toList());
            return Either.right(tickets.stream()
                    .filter(ticket -> bySpotId.containsKey(ticket.getSpotId()))
                    .flatMap(ticket -> customerGateway
                            .findById(ticket.getCustomerId())
                            .map(customer -> ListShowBuyersOutput.from(
                                    ticket, bySpotId.get(ticket.getSpotId()), customer))
                            .stream())
                    .toList());
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
