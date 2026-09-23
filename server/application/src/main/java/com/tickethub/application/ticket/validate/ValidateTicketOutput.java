package com.tickethub.application.ticket.validate;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.ticket.Ticket;
import java.time.Instant;
import java.time.OffsetDateTime;

public record ValidateTicketOutput(
        String showId,
        String ticketId,
        String code,
        String orderId,
        String spotId,
        String location,
        OffsetDateTime showDate,
        Instant checkedInAt) {
    public static ValidateTicketOutput from(
            final String showId, final Ticket ticket, final Spot spot, final OffsetDateTime showDate) {
        return new ValidateTicketOutput(
                showId,
                ticket.getId().getValue(),
                ticket.getCode(),
                ticket.getOrderId().getValue(),
                spot.getId().getValue(),
                spot.getLocation().getValue(),
                showDate,
                spot.getUpdatedAt());
    }
}
