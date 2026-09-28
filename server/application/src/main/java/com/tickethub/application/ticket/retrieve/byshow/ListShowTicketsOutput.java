package com.tickethub.application.ticket.retrieve.byshow;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.ticket.Ticket;

public record ListShowTicketsOutput(
        String ticketId,
        String code,
        String signature,
        String status,
        String spotId,
        String location) {
    public static ListShowTicketsOutput from(final Ticket ticket, final Spot spot) {
        return new ListShowTicketsOutput(
                ticket.getId().getValue(),
                ticket.getCode(),
                ticket.getSignature(),
                ticket.getStatus().name(),
                ticket.getSpotId().getValue(),
                spot.getLocation().getValue());
    }
}
