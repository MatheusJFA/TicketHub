package com.tickethub.infrastructure.ticket.models;

import com.tickethub.application.ticket.validate.ValidateTicketOutput;
import java.time.Instant;
import java.time.OffsetDateTime;

public record ValidateTicketResponse(
        String showId,
        String ticketId,
        String code,
        String orderId,
        String spotId,
        String location,
        OffsetDateTime showDate,
        Instant checkedInAt) {
    public static ValidateTicketResponse from(final ValidateTicketOutput output) {
        return new ValidateTicketResponse(
                output.showId(),
                output.ticketId(),
                output.code(),
                output.orderId(),
                output.spotId(),
                output.location(),
                output.showDate(),
                output.checkedInAt());
    }
}
