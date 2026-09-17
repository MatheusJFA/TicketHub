package com.tickethub.domain;

import java.util.Objects;
import java.time.Instant;

import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.validation.ValidationHandler;

public abstract class Entity<ID extends Identifier> {

    protected final ID id;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    protected Entity(final ID id) {
        final var now = Instant.now();
        this(id, now, now, null);
    }

    protected Entity(
            final ID id,
            final Instant createdAt,
            final Instant updatedAt,
            final Instant deletedAt
    ) {
        if (id == null) {
            throw new DomainException("'id' should not be null");
        }
        this.id = id;
        this.createdAt = Objects.requireNonNull(createdAt, "'createdAt' should not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "'updatedAt' should not be null");
        this.deletedAt = deletedAt;
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
        if (isDeleted()) {
            return;
        }
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Entity<?> other)) {
            return false;
        }

        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
