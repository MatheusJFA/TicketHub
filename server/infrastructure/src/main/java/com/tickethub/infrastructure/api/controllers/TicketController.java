package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.ticket.validate.ValidateTicketCommand;
import com.tickethub.application.ticket.validate.ValidateTicketUseCase;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.TicketAPI;
import com.tickethub.infrastructure.ticket.models.ValidateTicketRequest;
import com.tickethub.infrastructure.ticket.models.ValidateTicketResponse;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController implements TicketAPI {
    private final ValidateTicketUseCase validateTicket;

    public TicketController(final ValidateTicketUseCase validateTicket) {
        this.validateTicket = validateTicket;
    }

    @Override
    public ValidateTicketResponse validateTicket(final String showId, final ValidateTicketRequest input) {
        final var output = HttpResults.require(validateTicket.execute(
                ValidateTicketCommand.with(showId, input.ticketId(), input.code(), input.signature())));
        return ValidateTicketResponse.from(output);
    }
}
