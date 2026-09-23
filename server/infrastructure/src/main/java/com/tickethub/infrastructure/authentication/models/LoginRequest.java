package com.tickethub.infrastructure.authentication.models;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "must not be blank") String identifier,
        @NotBlank(message = "must not be blank") String password) {}
