package com.tickethub.infrastructure.ticket.models;

import com.tickethub.application.ticket.retrieve.bycustomer.ListCustomerTicketsOutput;
import java.time.OffsetDateTime;

public record CustomerTicketResponse(
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
    public static CustomerTicketResponse from(final ListCustomerTicketsOutput output) {
        return new CustomerTicketResponse(
                output.ticketId(),
                output.code(),
                output.signature(),
                output.status(),
                output.orderId(),
                output.spotId(),
                output.location(),
                output.showId(),
                output.showName(),
                output.showDate());
    }

    /** QR payload scanned at the door: {@code ticketId:code:signature}. */
    public String qrPayload() {
        return ticketId + ":" + code + ":" + signature;
    }
}
