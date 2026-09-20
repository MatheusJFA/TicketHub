package com.tickethub.domain;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.time.Instant;

import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.validation.ValidationHandler;
import java.util.Objects;

public abstract class Entity<ID extends Identifier> {

    protected final ID id;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private final String createdBy;
    private String lastModifiedBy;

    protected Entity(final ID id) {
        final var now = Instant.now();
        this(id, now, now, null, null, null);
    }

    protected Entity(
            final ID id,
            final Instant createdAt,
            final Instant updatedAt,
            final Instant deletedAt
    ) {
        this(id, createdAt, updatedAt, deletedAt, null, null);
    }

    protected Entity(
            final ID id,
            final Instant createdAt,
            final Instant updatedAt,
            final Instant deletedAt,
            final String createdBy,
            final String lastModifiedBy
    ) {
        if (isNull(id)) {
            throw new DomainException("'id' should not be null");
        }
        this.id = id;
        this.createdAt = requireNonNull(createdAt, "'createdAt' should not be null");
        this.updatedAt = requireNonNull(updatedAt, "'updatedAt' should not be null");
        this.deletedAt = deletedAt;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    protected void markAsUpdated() {
        this.updatedAt = Instant.now();
    }

    protected void markAsUpdatedBy(final String actor) {
        this.updatedAt = Instant.now();
        this.lastModifiedBy = actor;
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
        return nonNull(deletedAt);
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
