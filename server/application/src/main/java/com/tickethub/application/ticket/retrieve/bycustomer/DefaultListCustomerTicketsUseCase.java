package com.tickethub.application.ticket.retrieve.bycustomer;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.validation.Notification;
import java.util.List;

/**
 * Lists the tickets owned by a customer, enriched with the spot location and
 * the show they belong to. Tickets whose placement or show vanished are
 * skipped instead of failing the whole listing.
 */
public class DefaultListCustomerTicketsUseCase extends ListCustomerTicketsUseCase {
    private final TicketGateway ticketGateway;
    private final SpotGateway spotGateway;
    private final ShowGateway showGateway;

    public DefaultListCustomerTicketsUseCase(
            final TicketGateway ticketGateway,
            final SpotGateway spotGateway,
            final ShowGateway showGateway) {
        this.ticketGateway = requireNonNull(ticketGateway);
        this.spotGateway = requireNonNull(spotGateway);
        this.showGateway = requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, List<ListCustomerTicketsOutput>> execute(
            final ListCustomerTicketsCommand input) {
        try {
            final var tickets =
                    ticketGateway.findByCustomerId(CustomerID.from(input.customerId()));
            final var output =
                    tickets.stream()
                            .flatMap(
                                    ticket ->
                                            spotGateway
                                                    .findPlacement(ticket.getSpotId())
                                                    .flatMap(
                                                            placement ->
                                                                    showGateway
                                                                            .findById(
                                                                                    ShowID.from(
                                                                                            placement.showId()))
                                                                            .map(
                                                                                    show ->
                                                                                            ListCustomerTicketsOutput
                                                                                                    .from(
                                                                                                            ticket,
                                                                                                            placement,
                                                                                                            show)))
                                                    .stream())
                            .toList();
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
