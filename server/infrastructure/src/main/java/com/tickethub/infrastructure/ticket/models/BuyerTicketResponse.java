package com.tickethub.infrastructure.ticket.models;

import com.tickethub.application.ticket.retrieve.buyers.ListShowBuyersOutput;

public record BuyerTicketResponse(
        String ticketId,
        String status,
        String spotId,
        String location,
        String buyerName,
        String buyerCpf,
        String buyerEmail) {
    public static BuyerTicketResponse from(final ListShowBuyersOutput output) {
        return new BuyerTicketResponse(
                output.ticketId(),
                output.status(),
                output.spotId(),
                output.location(),
                output.buyerName(),
                output.buyerCpf(),
                output.buyerEmail());
    }
}
