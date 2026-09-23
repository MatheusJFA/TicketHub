package com.tickethub.domain.core.ticket;

import com.tickethub.domain.core.order.OrderID;
import java.util.List;
import java.util.Optional;

public interface TicketGateway {
    Ticket create(Ticket ticket);

    Optional<Ticket> findById(TicketID id);

    List<Ticket> findByOrderId(OrderID orderId);

    Ticket update(Ticket ticket);
}
