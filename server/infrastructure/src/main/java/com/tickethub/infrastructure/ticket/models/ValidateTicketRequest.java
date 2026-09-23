package com.tickethub.infrastructure.ticket.models;

import jakarta.validation.constraints.NotBlank;

/**
 * Body for ticket validation. The door scanner reads the guest QR code
 * (payload format {@code ticketId:code:signature}) and submits the three
 * parts; the path identifies the show being controlled.
 */
public record ValidateTicketRequest(
        @NotBlank String ticketId,
        @NotBlank String code,
        @NotBlank String signature) {}
