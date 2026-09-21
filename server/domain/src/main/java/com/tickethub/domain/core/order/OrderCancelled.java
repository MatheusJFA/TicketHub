package com.tickethub.domain.core.order;

import static java.util.Objects.requireNonNull;

import java.time.Instant;

import com.tickethub.domain.event.DomainEvent;

public record OrderCancelled(
        String orderId,
        Instant occurredOn) implements DomainEvent {

    public OrderCancelled {
        requireNonNull(orderId, "'orderId' should not be null");
        requireNonNull(occurredOn, "'occurredOn' should not be null");
    }
}
