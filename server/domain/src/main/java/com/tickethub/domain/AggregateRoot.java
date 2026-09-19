package com.tickethub.domain;

import com.tickethub.domain.event.DomainEvent;
import com.tickethub.domain.event.DomainEventPublisher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AggregateRoot<ID extends Identifier> extends Entity<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(final ID id) {
        super(id);
    }

    protected AggregateRoot(final ID id, final Instant createdAt, final Instant updatedAt,
            final Instant deletedAt, final String createdBy, final String lastModifiedBy) {
        super(id, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }

    public void registerEvent(final DomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public List<DomainEvent> getDomainEvents() {
        return domainEvents();
    }

    public void publishDomainEvents(final DomainEventPublisher publisher) {
        Objects.requireNonNull(publisher, "'publisher' should not be null");
        domainEvents.forEach(publisher::publish);
        domainEvents.clear();
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
