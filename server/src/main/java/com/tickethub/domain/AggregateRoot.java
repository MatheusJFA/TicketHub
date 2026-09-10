package com.tickethub.domain;

import com.tickethub.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot<ID extends Identifier> extends Entity<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(final ID id) {
        super(id);
    }

    public void registerEvent(final DomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
