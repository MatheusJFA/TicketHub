package com.tickethub.infrastructure.api;

import com.tickethub.infrastructure.authentication.models.LoginRequest;
import com.tickethub.infrastructure.authentication.models.LogoutRequest;
import com.tickethub.infrastructure.authentication.models.RefreshRequest;
import com.tickethub.infrastructure.authentication.models.SessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auth")
public interface AuthAPI {

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Authenticate with email and issue access + refresh tokens")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Authenticated successfully; returns access and refresh tokens"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON"),
        @ApiResponse(responseCode = "401", description = "Invalid identifier or password"),
        @ApiResponse(responseCode = "429", description = "Too many login attempts"),
        @ApiResponse(
                responseCode = "500",
                description = "An unexpected failure prevented the server from completing the operation")
    })
    ResponseEntity<SessionResponse> login(@Valid @RequestBody LoginRequest input);

    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rotate a refresh token and issue a new session")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Session rotated successfully; returns access and refresh tokens"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON"),
        @ApiResponse(responseCode = "401", description = "Invalid, expired or revoked refresh token"),
        @ApiResponse(
                responseCode = "500",
                description = "An unexpected failure prevented the server from completing the operation")
    })
    ResponseEntity<SessionResponse> refresh(@Valid @RequestBody RefreshRequest input);

    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Revoke a refresh token and optionally the access token")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Session revoked; unknown tokens are ignored"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON"),
        @ApiResponse(
                responseCode = "500",
                description = "An unexpected failure prevented the server from completing the operation")
    })
    ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest input);
}
