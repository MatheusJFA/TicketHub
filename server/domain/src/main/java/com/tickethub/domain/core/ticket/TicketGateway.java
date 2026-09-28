package com.tickethub.domain.core.ticket;

import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.spot.SpotID;
import java.util.List;
import java.util.Optional;

public interface TicketGateway {
    Ticket create(Ticket ticket);

    Optional<Ticket> findById(TicketID id);

    List<Ticket> findByOrderId(OrderID orderId);

    List<Ticket> findByCustomerId(CustomerID customerId);

    /**
     * Loads tickets for a batch of spots, for door preload
     * ({@code GET /shows/{showId}/tickets}). Returns empty for an empty input.
     */
    List<Ticket> findBySpotIds(List<SpotID> spotIds);

    Ticket update(Ticket ticket);
}
