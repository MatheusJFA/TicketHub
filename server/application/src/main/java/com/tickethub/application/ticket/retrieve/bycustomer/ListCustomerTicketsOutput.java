package com.tickethub.application.ticket.retrieve.bycustomer;

import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.spot.SpotPlacement;
import com.tickethub.domain.core.ticket.Ticket;
import java.time.OffsetDateTime;

public record ListCustomerTicketsOutput(
        String ticketId,
        String code,
        String signature,
        String status,
        String orderId,
        String spotId,
        String location,
        String showId,
        String showName,
        OffsetDateTime showDate) {
    public static ListCustomerTicketsOutput from(
            final Ticket ticket, final SpotPlacement placement, final Show show) {
        return new ListCustomerTicketsOutput(
                ticket.getId().getValue(),
                ticket.getCode(),
                ticket.getSignature(),
                ticket.getStatus().name(),
                ticket.getOrderId().getValue(),
                ticket.getSpotId().getValue(),
                placement.spot().getLocation().getValue(),
                placement.showId(),
                show.getName().getValue(),
                show.getDate());
    }
}
