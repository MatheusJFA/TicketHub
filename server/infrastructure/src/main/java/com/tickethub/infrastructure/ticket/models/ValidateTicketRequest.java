package com.tickethub.infrastructure.ticket.models;

import jakarta.validation.constraints.NotBlank;

/**
 * Body for ticket validation. The door scanner reads the guest QR code
 * (payload format {@code showId:sectionId:spotId}) and submits the three
 * identifiers; the path identifies the show being controlled.
 */
public record ValidateTicketRequest(
        @NotBlank String sectionId,
        @NotBlank String spotId) {
}
