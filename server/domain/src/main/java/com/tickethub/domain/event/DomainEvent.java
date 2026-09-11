package com.tickethub.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
