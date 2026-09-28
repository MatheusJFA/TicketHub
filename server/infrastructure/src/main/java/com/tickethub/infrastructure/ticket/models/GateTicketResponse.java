package com.tickethub.infrastructure.ticket.models;

import com.tickethub.application.ticket.retrieve.byshow.ListShowTicketsOutput;

public record GateTicketResponse(
        String ticketId,
        String code,
        String signature,
        String status,
        String spotId,
        String location) {
    public static GateTicketResponse from(final ListShowTicketsOutput output) {
        return new GateTicketResponse(
                output.ticketId(),
                output.code(),
                output.signature(),
                output.status(),
                output.spotId(),
                output.location());
    }
}
