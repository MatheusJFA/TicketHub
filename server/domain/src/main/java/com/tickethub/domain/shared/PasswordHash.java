package com.tickethub.domain.shared;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.tickethub.domain.ValueObject;
import com.tickethub.domain.exception.DomainException;
import java.util.Objects;
import java.util.regex.Pattern;

public final class PasswordHash extends ValueObject {
    // Filtra: hashes bcrypt ($2a/$2b/$2y + custo de 2 digitos + 53 caracteres).
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private final String value;

    private PasswordHash(String value) {
        this.value = value;
    }

    public static PasswordHash fromHash(String value) {
        if (isBlank(value) || !BCRYPT_PATTERN.matcher(value).matches()) {
            throw new DomainException("Invalid password hash");
        }

        return new PasswordHash(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof PasswordHash hash && Objects.equals(value, hash.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "*".repeat(100);
    }
}
