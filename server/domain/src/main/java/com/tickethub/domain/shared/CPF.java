package com.tickethub.domain.shared;

import static java.util.Objects.isNull;

import com.tickethub.domain.ValueObject;
import com.tickethub.domain.exception.DomainException;
import java.util.Objects;

public final class CPF extends ValueObject {
    private static final int CPF_LENGTH = 11;
    private static final String EMPTY_STRING = "";

    private final String value;

    private CPF(String value) {
        this.value = value;
    }

    public static CPF create(String value) {
        String normalizedValue = normalize(value);

        if (!isValid(normalizedValue)) {
            throw new DomainException("Invalid CPF");
        }

        return new CPF(normalizedValue);
    }

    private static boolean isValid(String cpf) {
        cpf = normalize(cpf);

        if (isNull(cpf)) {
            return false;
        }

        if (cpf.length() != CPF_LENGTH || hasAllDigitsEqual(cpf)) {
            return false;
        }

        int firstDigit = calculateDigit(cpf.substring(0, 9), 10);
        int secondDigit = calculateDigit(cpf.substring(0, 10), 11);

        return firstDigit == Character.getNumericValue(cpf.charAt(9))
                && secondDigit == Character.getNumericValue(cpf.charAt(10));
    }

    private static int calculateDigit(String digits, int weight) {
        int sum = 0;

        for (int i = 0; i < digits.length(); i++) {
            int digit = Character.getNumericValue(digits.charAt(i));
            sum += digit * (weight - i);
        }

        int remainder = sum % 11;

        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static boolean hasAllDigitsEqual(String cpf) {
        return cpf.matches("(\\d)\\1{10}");
    }

    private static String normalize(String value) {
        if (isNull(value)) {
            return null;
        }

        return value.replaceAll("\\D", EMPTY_STRING);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CPF cpf && value.equals(cpf.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.replaceFirst(
                "(\\d{3})(\\d{3})(\\d{3})(\\d{2})",
                "$1.$2.$3-$4");
    }

}
