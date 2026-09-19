package com.tickethub.infrastructure.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tickethub.infrastructure.auth.models.LoginRequest;
import com.tickethub.infrastructure.auth.models.TokenResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auth")
public interface AuthAPI {

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Authenticate and issue a JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated successfully; returns a bearer token"),
            @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password"),
            @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation")
    })
    ResponseEntity<TokenResponse> login(@RequestBody LoginRequest input);
}
