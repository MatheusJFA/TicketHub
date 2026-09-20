package com.tickethub.infrastructure.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tickethub.infrastructure.zipcode.models.ZipCodeResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "ZIP code")
public interface ZipCodeAPI {
    @GetMapping(value = "/zipcode/{zipCode}")
    @Operation(summary = "Lookup ZIP code",
            description = "Looks up address data for a zip code so clients can autofill address forms before registration. Create/update flows persist the submitted address as-is and never call this provider.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Address data found for the zip code"),
        @ApiResponse(responseCode = "404", description = "No address data found for the supplied zip code"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation")
    })
    ZipCodeResponse lookup(@PathVariable("zipCode") String zipCode);
}
