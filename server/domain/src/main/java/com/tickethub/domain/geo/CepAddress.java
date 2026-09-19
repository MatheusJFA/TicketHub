package com.tickethub.domain.geo;

import org.apache.commons.lang3.StringUtils;

public record CepAddress(String zipCode, String street, String neighborhood, String city, String state,
        String country) {

    public CepAddress {
        if (StringUtils.isBlank(zipCode)) {
            throw new IllegalArgumentException("'zipCode' should not be null or blank");
        }
    }

    public static String normalize(final String zipCode) {
        final String digits = StringUtils.defaultString(zipCode).replaceAll("\\D", "");
        return digits.length() == 8 ? digits : null;
    }
}
