package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.ticket.retrieve.bycustomer.ListCustomerTicketsCommand;
import com.tickethub.application.ticket.retrieve.bycustomer.ListCustomerTicketsUseCase;
import com.tickethub.application.ticket.retrieve.byshow.ListShowTicketsCommand;
import com.tickethub.application.ticket.retrieve.byshow.ListShowTicketsUseCase;
import com.tickethub.application.ticket.validate.ValidateTicketCommand;
import com.tickethub.application.ticket.validate.ValidateTicketUseCase;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.TicketAPI;
import com.tickethub.infrastructure.ticket.models.CustomerTicketResponse;
import com.tickethub.infrastructure.ticket.models.GateTicketResponse;
import com.tickethub.infrastructure.ticket.models.ValidateTicketRequest;
import com.tickethub.infrastructure.ticket.models.ValidateTicketResponse;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController implements TicketAPI {
    private final ValidateTicketUseCase validateTicket;
    private final ListCustomerTicketsUseCase listCustomerTickets;
    private final ListShowTicketsUseCase listShowTickets;

    public TicketController(
            final ValidateTicketUseCase validateTicket,
            final ListCustomerTicketsUseCase listCustomerTickets,
            final ListShowTicketsUseCase listShowTickets) {
        this.validateTicket = validateTicket;
        this.listCustomerTickets = listCustomerTickets;
        this.listShowTickets = listShowTickets;
    }

    @Override
    public ValidateTicketResponse validateTicket(final String showId, final ValidateTicketRequest input) {
        final var output = HttpResults.require(validateTicket.execute(
                ValidateTicketCommand.with(showId, input.ticketId(), input.code(), input.signature())));
        return ValidateTicketResponse.from(output);
    }

    @Override
    public List<CustomerTicketResponse> listCustomerTickets(final String customerId) {
        final var output = HttpResults.require(
                listCustomerTickets.execute(ListCustomerTicketsCommand.with(customerId)));
        return output.stream().map(CustomerTicketResponse::from).toList();
    }

    @Override
    public List<GateTicketResponse> listShowTickets(final String showId) {
        final var output = HttpResults.require(
                listShowTickets.execute(ListShowTicketsCommand.with(showId)));
        return output.stream().map(GateTicketResponse::from).toList();
    }
}
