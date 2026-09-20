package com.tickethub.domain.shared;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

    /**
     * Derives the section code from its zero-based position within the show,
     * spreadsheet style: 0 → A … 25 → Z, 26 → AA, 27 → AB …
     */
    public static String sectionCode(int sectionIndex) {
        if (sectionIndex < 0) {
            throw new DomainException("'sectionIndex' should not be negative");
        }
        final StringBuilder code = new StringBuilder();
        int index = sectionIndex;
        do {
            code.insert(0, (char) ('A' + index % 26));
            index = index / 26 - 1;
        } while (index >= 0);
        return code.toString();
    }

    /**
     * Generates a short seat code such as {@code A00001}: the section code
     * plus the one-based seat number, zero-padded to the configured width.
     * The width always comes from the caller (see
     * {@code tickethub.spots.seat-number-width}); it is never assumed here.
     */
    public static Location generateSeat(String sectionCode, long seatNumber, int seatNumberWidth) {
        if (isNull(sectionCode) || !sectionCode.matches("[A-Z]{1,3}")) {
            throw new DomainException("Invalid section code");
        }
        if (seatNumber < 1) {
            throw new DomainException("'seatNumber' should be positive");
        }
        if (seatNumberWidth < 1) {
            throw new DomainException("'seatNumberWidth' should be positive");
        }
        return new Location(sectionCode + String.format("%0" + seatNumberWidth + "d", seatNumber));
    }

    /**
     * Generates a code for spots created outside any section, derived from a
     * random hash so orphan spots still carry a short human-readable code.
     */
    public static Location generateRandom(int seatNumberWidth) {
        if (seatNumberWidth < 1) {
            throw new DomainException("'seatNumberWidth' should be positive");
        }
        final int hash = java.util.UUID.randomUUID().hashCode();
        final int positive = hash == Integer.MIN_VALUE ? 0 : Math.abs(hash);
        final char letter = (char) ('A' + positive % 26);
        final long bound = (long) Math.pow(10, seatNumberWidth);
        final long number = positive / 26 % bound + 1;
        return new Location(letter + String.format("%0" + seatNumberWidth + "d", number));
    }

    private static boolean isValid(String value) {
        return isNotBlank(value);
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
