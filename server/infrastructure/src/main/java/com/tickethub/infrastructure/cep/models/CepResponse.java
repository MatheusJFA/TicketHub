package com.tickethub.infrastructure.cep.models;

import com.tickethub.application.cep.lookup.LookupCepOutput;

public record CepResponse(
        String zipCode,
        String street,
        String neighborhood,
        String city,
        String state,
        String country) {
    public static CepResponse from(final LookupCepOutput output) {
        return new CepResponse(
                output.zipCode(),
                output.street(),
                output.neighborhood(),
                output.city(),
                output.state(),
                output.country());
    }
}
