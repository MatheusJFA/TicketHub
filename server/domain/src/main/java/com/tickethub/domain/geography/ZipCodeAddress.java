package com.tickethub.domain.geography;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;

public record ZipCodeAddress(
        String zipCode, String street, String neighborhood, String city, String state, String country) {

    public ZipCodeAddress {
        if (isBlank(zipCode)) {
            throw new IllegalArgumentException("'zipCode' should not be null or blank");
        }
    }

    public static String normalize(final String zipCode) {
        // Filtra: tudo que nao e digito para normalizar o CEP so com numeros.
        final String digits = defaultString(zipCode).replaceAll("\\D", "");
        return digits.length() == 8 ? digits : null;
    }
}
