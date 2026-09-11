package com.tickethub.domain.shared;

import static java.util.Objects.isNull;

import com.tickethub.domain.ValueObject;
import com.tickethub.domain.exception.DomainException;

public final class CNPJ extends ValueObject {
    private static final int CNPJ_LENGTH = 14;
    private static final String EMPTY_STRING = "";

    private final String value;

    private CNPJ(String value) {
        this.value = value;
    }

    public static CNPJ create(String value) {
        String normalizedValue = normalize(value);

        if (!isValid(normalizedValue)) {
            throw new DomainException("Invalid CNPJ");
        }

        return new CNPJ(normalizedValue);
    }

    private static boolean isValid(String cnpj) {
        if (isNull(cnpj) || cnpj.length() != CNPJ_LENGTH) {
            return false;
        }

        if (!cnpj.matches("[A-Z0-9]{12}[0-9]{2}") || hasAllDigitsEqual(cnpj)) {
            return false;
        }

        int firstDigit = calculateDigit(cnpj.substring(0, 12));
        int secondDigit = calculateDigit(cnpj.substring(0, 13));

        return firstDigit == cnpj.charAt(12) - '0'
                && secondDigit == cnpj.charAt(13) - '0';
    }

    private static int calculateDigit(String value) {
        int sum = 0;
        int weight = 2;

        for (int i = value.length() - 1; i >= 0; i--) {
            // The official CNPJ rule maps each character to its ASCII value minus 48.
            int digit = value.charAt(i) - '0';
            sum += digit * weight;
            weight = weight == 9 ? 2 : weight + 1;
        }

        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static boolean hasAllDigitsEqual(String cnpj) {
        return cnpj.matches("(\\d)\\1{13}");
    }

    private static String normalize(String value) {
        if (isNull(value)) {
            return null;
        }

        return value.replaceAll("[./\\s-]", EMPTY_STRING);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CNPJ cnpj && value.equals(cnpj.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.replaceFirst(
                "([A-Z0-9]{2})([A-Z0-9]{3})([A-Z0-9]{3})([A-Z0-9]{4})([0-9]{2})",
                "$1.$2.$3/$4-$5");
    }
}
