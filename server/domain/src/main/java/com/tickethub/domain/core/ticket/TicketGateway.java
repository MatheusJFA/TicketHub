package com.tickethub.domain.core.ticket;

import java.util.List;
import java.util.Optional;

import com.tickethub.domain.core.order.OrderID;

public interface TicketGateway {
    Ticket create(Ticket ticket);
    Optional<Ticket> findById(TicketID id);
    List<Ticket> findByOrderId(OrderID orderId);
    Ticket update(Ticket ticket);
}
