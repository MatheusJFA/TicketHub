package com.tickethub.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.time.Instant;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import com.tickethub.domain.event.DomainEvent;
import com.tickethub.domain.event.DomainEventPublisher;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.validation.ValidationHandler;

public abstract class Entity<ID extends Identifier> {

    protected final ID id;
    private final List<DomainEvent> domainEvents;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    protected Entity(final ID id) {
        this(id, Instant.now(), Instant.now(), null, null);
    }

    protected Entity(final ID id, final List<DomainEvent> domainEvents) {
        this(id, Instant.now(), Instant.now(), null, domainEvents);
    }

    protected Entity(
            final ID id,
            final Instant createdAt,
            final Instant updatedAt,
            final Instant deletedAt,
            final List<DomainEvent> domainEvents
    ) {
        if (id == null) {
            throw new DomainException("'id' should not be null");
        }
        this.id = id;
        this.createdAt = requireNonNull(createdAt, "'createdAt' should not be null");
        this.updatedAt = requireNonNull(updatedAt, "'updatedAt' should not be null");
        this.deletedAt = deletedAt;
        this.domainEvents = new ArrayList<>(Objects.isNull(domainEvents) ? Collections.emptyList() : domainEvents);
    }

    public ID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    protected void markAsUpdated() {
        this.updatedAt = Instant.now();
    }

    public void delete() {
        this.deletedAt = Instant.now();
        markAsUpdated();
    }

    public void restore() {
        this.deletedAt = null;
        markAsUpdated();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void validate(final ValidationHandler handler) {
        // Entities without specific invariants do not need validation.
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void publishDomainEvents(final DomainEventPublisher publisher) {
        if (isNull(publisher)) {
            return;
        }

        domainEvents.forEach(publisher::publish);
        domainEvents.clear();
    }

    public void addDomainEvent(final DomainEvent event) {
        if (isNull(event)) {
            return;
        }

        domainEvents.add(event);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (Objects.isNull(o) || getClass() != o.getClass()) {
            return false;
        }

        return id.equals(((Entity<?>) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
