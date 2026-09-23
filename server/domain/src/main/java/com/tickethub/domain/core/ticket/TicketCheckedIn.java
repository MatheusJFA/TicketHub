package com.tickethub.domain.core.ticket;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.event.DomainEvent;
import java.time.Instant;

public record TicketCheckedIn(String ticketId, String orderId, Instant occurredOn) implements DomainEvent {

    public TicketCheckedIn {
        requireNonNull(ticketId, "'ticketId' should not be null");
        requireNonNull(orderId, "'orderId' should not be null");
        requireNonNull(occurredOn, "'occurredOn' should not be null");
    }
}
