package com.tickethub.domain.core.ticket;

import java.util.Optional;

public interface TicketGateway {
    Ticket create(Ticket ticket);
    Optional<Ticket> findById(TicketID id);
    Ticket update(Ticket ticket);
}
