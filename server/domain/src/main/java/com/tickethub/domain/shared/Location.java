package com.tickethub.domain.shared;

import static java.util.Objects.isNull;

import com.tickethub.domain.ValueObject;
import com.tickethub.domain.exception.DomainException;

public final class Location extends ValueObject {
    private static final String SPACE = " ";

    private final String value;

    private Location(String value) {
        this.value = value;
    }

    public static Location create(String value) {
        String normalizedValue = normalize(value);

        if (!isValid(normalizedValue)) {
            throw new DomainException("Invalid location");
        }

        return new Location(normalizedValue);
    }

    private static boolean isValid(String value) {
        return value != null && !value.isBlank();
    }

    private static String normalize(String value) {
        if (isNull(value)) {
            return null;
        }

        return value.replaceAll("(?U)\\s+", SPACE).strip();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Location location && value.equals(location.value);
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
