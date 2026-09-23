package com.tickethub.domain.shared;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.tickethub.domain.ValueObject;
import com.tickethub.domain.exception.DomainException;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Email extends ValueObject {
    private static final int MAX_LENGTH = 254;
    // Filtra: e-mails no formato local@dominio.tld (parte local + @ + dominio + . + TLD com 2+ letras).
    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email create(String value) {
        final String normalizedValue = normalize(value);

        if (!isValid(normalizedValue)) {
            throw new DomainException("Invalid email");
        }

        return new Email(normalizedValue);
    }

    private static boolean isValid(String email) {
        if (isBlank(email) || email.length() > MAX_LENGTH) {
            return false;
        }

        if (!PATTERN.matcher(email).matches()) {
            return false;
        }

        // Filtra: separa a parte local do dominio pelo "@".
        final String[] parts = email.split("@", -1);
        if (parts.length != 2) {
            return false;
        }

        final String local = parts[0];
        final String domain = parts[1];

        if (local.startsWith(".") || local.endsWith(".") || local.contains("..")) {
            return false;
        }

        if (domain.startsWith(".") || domain.startsWith("-") || domain.contains("..")) {
            return false;
        }

        return true;
    }

    private static String normalize(String value) {
        if (isNull(value)) {
            return null;
        }

        return value.trim().toLowerCase();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Email email && Objects.equals(value, email.value);
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
