package com.tickethub.domain.geo;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;

public record CepAddress(String zipCode, String street, String neighborhood, String city, String state,
        String country) {

    public CepAddress {
        if (isBlank(zipCode)) {
            throw new IllegalArgumentException("'zipCode' should not be null or blank");
        }
    }

    public static String normalize(final String zipCode) {
        final String digits = defaultString(zipCode).replaceAll("\\D", "");
        return digits.length() == 8 ? digits : null;
    }
}
