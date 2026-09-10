package com.tickethub.domain;

import java.util.Objects;

import com.tickethub.domain.exception.DomainException;

public abstract class Entity<ID extends Identifier> {

    protected final ID id;

    protected Entity(final ID id) {
        if (Objects.isNull(id)) {
            throw new DomainException("'id' should not be null");
        }
        this.id = id;
    }

    public ID getId() {
        return id;
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
