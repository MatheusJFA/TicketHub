package com.tickethub.domain.core.order;

import static java.util.Objects.requireNonNull;

import java.time.Instant;

import com.tickethub.domain.event.DomainEvent;
import com.tickethub.domain.shared.Money;

public record OrderCreated(
        String orderId,
        String customerId,
        Money total,
        Instant expiresAt,
        Instant occurredOn) implements DomainEvent {

    public OrderCreated {
        requireNonNull(orderId, "'orderId' should not be null");
        requireNonNull(customerId, "'customerId' should not be null");
        requireNonNull(total, "'total' should not be null");
        requireNonNull(expiresAt, "'expiresAt' should not be null");
        requireNonNull(occurredOn, "'occurredOn' should not be null");
    }
}
