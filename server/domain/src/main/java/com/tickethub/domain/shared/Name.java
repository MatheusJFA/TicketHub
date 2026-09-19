package com.tickethub.domain.shared;

import static java.util.Objects.isNull;

import com.tickethub.domain.ValueObject;
import com.tickethub.domain.exception.DomainException;

import java.util.Objects;


public final class Name extends ValueObject {
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;
    private static final String SPACE = " ";

    private final String value;

    private Name(String value) {
        this.value = value;
    }

    public static Name create(String value) {
        String normalizedValue = normalize(value);

        if (!isValid(normalizedValue)) {
            throw new DomainException("Invalid name " + value);
        }

        return new Name(normalizedValue);
    }

    private static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String name = normalize(value);

        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            return false;
        }

        return name.matches("[\\p{L}\\p{M} .'-]+");
    }

    private static String normalize(String value) {
        if (isNull(value)) {
            return null;
        }

        return value
                .trim()
                .replaceAll("\\s+", SPACE);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Name name && Objects.equals(value, name.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

}
