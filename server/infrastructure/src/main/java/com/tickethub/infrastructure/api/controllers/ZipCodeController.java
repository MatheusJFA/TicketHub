package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.zipcode.lookup.LookupZipCodeUseCase;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.ZipCodeAPI;
import com.tickethub.infrastructure.zipcode.models.ZipCodeResponse;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ZipCodeController implements ZipCodeAPI {
    private final LookupZipCodeUseCase lookupZipCode;

    public ZipCodeController(final LookupZipCodeUseCase lookupZipCode) {
        this.lookupZipCode = lookupZipCode;
    }

    @Override
    public ZipCodeResponse lookup(final String zipCode) {
        return ZipCodeResponse.from(HttpResults.require(lookupZipCode.execute(zipCode)));
    }
}
