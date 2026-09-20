package com.tickethub.application.ticket.validate;

/**
 * Data read from the guest QR code at the door: the show, section and spot
 * the ticket was issued for. Scanners parse the QR payload (format
 * {@code showId:sectionId:spotId}) and submit the three identifiers.
 */
public record ValidateTicketCommand(String showId, String sectionId, String spotId) {
    public static ValidateTicketCommand with(final String showId, final String sectionId,
            final String spotId) {
        return new ValidateTicketCommand(showId, sectionId, spotId);
    }
}
