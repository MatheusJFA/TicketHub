package com.tickethub.infrastructure.zipcode.models;

import com.tickethub.application.zipcode.lookup.LookupZipCodeOutput;

public record ZipCodeResponse(
        String zipCode, String street, String neighborhood, String city, String state, String country) {
    public static ZipCodeResponse from(final LookupZipCodeOutput output) {
        return new ZipCodeResponse(
                output.zipCode(),
                output.street(),
                output.neighborhood(),
                output.city(),
                output.state(),
                output.country());
    }
}
