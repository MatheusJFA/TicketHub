package com.tickethub.infrastructure.api;

import com.tickethub.infrastructure.ticket.models.CustomerTicketResponse;
import com.tickethub.infrastructure.ticket.models.GateTicketResponse;
import com.tickethub.infrastructure.ticket.models.ValidateTicketRequest;
import com.tickethub.infrastructure.ticket.models.ValidateTicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tickets")
public interface TicketAPI {
    @PostMapping(value = "/shows/{showId}/tickets/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Validate Ticket",
            description =
                    "Validates a ticket QR code at the door: proves the signature was issued by this system, confirms the ticket is unused and belongs to the show being controlled, that the show is happening today, and checks ticket and spot in so the QR cannot be reused")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Ticket is valid for this show; returns the checked-in ticket details"),
        @ApiResponse(
                responseCode = "400",
                description =
                        "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(
                responseCode = "404",
                description = "The ticket, spot or show was not found for the supplied identifiers"),
        @ApiResponse(
                responseCode = "422",
                description =
                        "The signature is invalid, the ticket was already used, belongs to another show, or the show is outside the check-in date"),
        @ApiResponse(
                responseCode = "500",
                description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(
                responseCode = "503",
                description =
                        "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('ticket:validate') and @showAccess.canWrite(#showId)")
    ValidateTicketResponse validateTicket(
            @PathVariable("showId") String showId, @Valid @RequestBody ValidateTicketRequest input);

    @GetMapping(value = "/tickets")
    @Operation(
            summary = "List Customer Tickets",
            description =
                    "Returns the tickets owned by a customer, enriched with spot location and show details; used by the 'my tickets' screen and to rebuild the door QR payload")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer tickets"),
        @ApiResponse(
                responseCode = "404",
                description = "The customer was not found for the supplied identifier"),
        @ApiResponse(
                responseCode = "500",
                description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(
                responseCode = "503",
                description =
                        "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('order:write') and @ownerAccess.isSelfOrAdmin(#customerId)")
    List<CustomerTicketResponse> listCustomerTickets(@RequestParam("customerId") String customerId);

    @GetMapping(value = "/shows/{showId}/tickets")
    @Operation(
            summary = "List Show Tickets",
            description =
                    "Returns every ticket of a show (id, code, signature, status, spot) for door preload, so the gate keeps matching ticketId:code while offline. Signature verification still happens server-side on validate")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show tickets"),
        @ApiResponse(responseCode = "404", description = "The show was not found"),
        @ApiResponse(
                responseCode = "500",
                description = "An unexpected failure prevented the server from completing the operation")
    })
    @PreAuthorize("hasAuthority('ticket:validate') and @showAccess.canWrite(#showId)")
    List<GateTicketResponse> listShowTickets(@PathVariable("showId") String showId);
}
