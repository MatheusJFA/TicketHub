package com.tickethub.domain.core.order;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.event.DomainEvent;
import java.time.Instant;

public record OrderExpired(String orderId, Instant occurredOn) implements DomainEvent {

    public OrderExpired {
        requireNonNull(orderId, "'orderId' should not be null");
        requireNonNull(occurredOn, "'occurredOn' should not be null");
    }
}
