package com.tickethub.application.ticket.retrieve.buyers;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.ticket.Ticket;

public record ListShowBuyersOutput(
        String ticketId,
        String status,
        String spotId,
        String location,
        String buyerName,
        String buyerCpf,
        String buyerEmail) {
    public static ListShowBuyersOutput from(
            final Ticket ticket, final Spot spot, final Customer customer) {
        return new ListShowBuyersOutput(
                ticket.getId().getValue(),
                ticket.getStatus().name(),
                ticket.getSpotId().getValue(),
                spot.getLocation().getValue(),
                customer.getName().getValue(),
                customer.getCpf().getValue(),
                customer.getEmail().getValue());
    }
}
