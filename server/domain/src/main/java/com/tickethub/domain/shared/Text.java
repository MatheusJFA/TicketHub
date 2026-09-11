package com.tickethub.domain.shared;

import static java.util.Objects.isNull;

import com.tickethub.domain.ValueObject;
import com.tickethub.domain.exception.DomainException;


public final class Text extends ValueObject {
    private static final int MAX_LENGTH = 1000;

    private final String value;

    private Text(String value) {
        this.value = value;
    }

    public static Text create(String value) {
        if (!isValid(value)) {
            throw new DomainException("Invalid text " + value);
        }

        return new Text(value);
    }

    private static boolean isValid(String value) {
        if (isNull(value) || value.isBlank()) {
            return true;
        }

        return value.length() <= MAX_LENGTH;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Text text && value.equals(text.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
