package com.tickethub.domain;

import static java.util.Objects.isNull;

import java.util.Objects;

public abstract class Identifier extends ValueObject {

    public abstract String getValue();

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (isNull(other) || getClass() != other.getClass()) {
            return false;
        }

        Identifier identifier = (Identifier) other;
        return Objects.equals(getValue(), identifier.getValue());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getValue());
    }
}
