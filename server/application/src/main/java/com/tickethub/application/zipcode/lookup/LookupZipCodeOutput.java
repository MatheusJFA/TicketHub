package com.tickethub.application.zipcode.lookup;

import com.tickethub.domain.geography.ZipCodeAddress;

public record LookupZipCodeOutput(
        String zipCode, String street, String neighborhood, String city, String state, String country) {
    public static LookupZipCodeOutput from(final ZipCodeAddress address) {
        return new LookupZipCodeOutput(
                address.zipCode(),
                address.street(),
                address.neighborhood(),
                address.city(),
                address.state(),
                address.country());
    }
}
