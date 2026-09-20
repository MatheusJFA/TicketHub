package com.tickethub.application.cep.lookup;

import com.tickethub.domain.geo.CepAddress;

public record LookupCepOutput(
        String zipCode,
        String street,
        String neighborhood,
        String city,
        String state,
        String country) {
    public static LookupCepOutput from(final CepAddress address) {
        return new LookupCepOutput(
                address.zipCode(),
                address.street(),
                address.neighborhood(),
                address.city(),
                address.state(),
                address.country());
    }
}
