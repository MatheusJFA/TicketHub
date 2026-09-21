package com.tickethub.application.ticket.validate;

/**
 * Data read from the guest QR code at the door: the ticket identity, its
 * human-readable code and its issuance signature ({@code ticketId:code}
 * signed). The path identifies the show being controlled.
 */
public record ValidateTicketCommand(String showId, String ticketId, String code, String signature) {
    public static ValidateTicketCommand with(final String showId, final String ticketId,
            final String code, final String signature) {
        return new ValidateTicketCommand(showId, ticketId, code, signature);
    }
}
