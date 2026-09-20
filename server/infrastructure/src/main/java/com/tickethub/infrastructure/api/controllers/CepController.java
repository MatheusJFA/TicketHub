package com.tickethub.infrastructure.api.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.tickethub.application.cep.lookup.LookupCepUseCase;
import com.tickethub.infrastructure.api.CepAPI;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.cep.models.CepResponse;

@RestController
public class CepController implements CepAPI {
    private final LookupCepUseCase lookupCep;

    public CepController(final LookupCepUseCase lookupCep) {
        this.lookupCep = lookupCep;
    }

    @Override
    public CepResponse lookup(final String zipCode) {
        return CepResponse.from(HttpResults.require(lookupCep.execute(zipCode)));
    }
}
