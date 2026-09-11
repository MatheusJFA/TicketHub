package com.tickethub.domain.event;

@FunctionalInterface
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
