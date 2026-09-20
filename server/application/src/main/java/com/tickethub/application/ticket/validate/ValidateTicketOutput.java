package com.tickethub.application.ticket.validate;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.tickethub.domain.core.spot.Spot;

public record ValidateTicketOutput(
        String showId,
        String sectionId,
        String spotId,
        String location,
        OffsetDateTime showDate,
        Instant checkedInAt) {
    public static ValidateTicketOutput from(final String showId, final String sectionId,
            final Spot spot, final OffsetDateTime showDate) {
        return new ValidateTicketOutput(
                showId,
                sectionId,
                spot.getId().getValue(),
                spot.getLocation().getValue(),
                showDate,
                spot.getUpdatedAt());
    }
}
