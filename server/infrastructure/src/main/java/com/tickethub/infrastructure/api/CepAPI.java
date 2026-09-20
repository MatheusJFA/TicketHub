package com.tickethub.infrastructure.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tickethub.infrastructure.cep.models.CepResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "CEP")
public interface CepAPI {
    @GetMapping(value = "/cep/{zipCode}")
    @Operation(summary = "Lookup CEP",
            description = "Looks up address data for a zip code so clients can autofill address forms before registration. Create/update flows persist the submitted address as-is and never call this provider.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Address data found for the zip code"),
        @ApiResponse(responseCode = "404", description = "No address data found for the supplied zip code"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation")
    })
    CepResponse lookup(@PathVariable("zipCode") String zipCode);
}
